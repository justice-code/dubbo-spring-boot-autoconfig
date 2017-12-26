package org.eddy.config;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getAnnotation;

public class DubboReferenceInjector extends InstantiationAwareBeanPostProcessorAdapter implements ApplicationContextAware{

    private ApplicationContext applicationContext;
    private final ConcurrentMap<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<String, InjectionMetadata>(256);

    public ReferenceBean configAndBuild(Reference reference, Class<?> interfaceClass) throws Exception {
        ReferenceBean referenceBean = new ReferenceBean(reference);

        referenceBean.setApplication(findBean(reference.application(), ApplicationConfig.class));
        referenceBean.setRegistries(findBean(reference.registry(), RegistryConfig.class));
        referenceBean.setModule(findBean(reference.module(), ModuleConfig.class));
        referenceBean.setApplicationContext(applicationContext);
        referenceBean.setInterface(interfaceClass);
        referenceBean.afterPropertiesSet();

        return referenceBean;
    }

    @Override
    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findReferenceMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @Reference dependencies failed", ex);
        }
        return pvs;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private <T> List<T> findBean(String[] names, Class<T> type) {
        return Arrays.stream(names).map(s -> applicationContext.getBean(s, type)).collect(Collectors.toList());
    }

    private <T> T findBean(String name, Class<T> type) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        return applicationContext.getBean(name, type);
    }

    private InjectionMetadata findReferenceMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildReferenceMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect bean class [" + clazz.getName() +
                                "] for reference metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildReferenceMetadata(final Class<?> beanClass) {
        final List<InjectionMetadata.InjectedElement> elements = new LinkedList<InjectionMetadata.InjectedElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                Reference reference = getAnnotation(field, Reference.class);

                if (reference != null) {

                    if (Modifier.isStatic(field.getModifiers())) {
                        return;
                    }

                    elements.add(new ReferenceInjectedElement(field, null, reference));
                }

            }
        });

        ReflectionUtils.doWithLocalMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                Method bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
                if (!BridgeMethodResolver.isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }
                Reference reference = findAnnotation(bridgedMethod, Reference.class);
                if (reference != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                    if (Modifier.isStatic(method.getModifiers())) {
                        return;
                    }
                    PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                    elements.add(new ReferenceInjectedElement(method, pd, reference));
                }
            }
        });



        return new InjectionMetadata(beanClass, elements);

    }

    private class ReferenceInjectedElement extends InjectionMetadata.InjectedElement {

        private Reference reference;

        protected ReferenceInjectedElement(Member member, PropertyDescriptor pd, Reference reference) {
            super(member, pd);
            this.reference = reference;
        }

        @Override
        protected Object getResourceToInject(Object target, String requestingBeanName) {
            try {
                return configAndBuild(reference, findInterfaceClass()).getObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Class findInterfaceClass() {
            if (super.isField && ((Field)super.member).getType().isInterface()) {
                return ((Field)super.member).getType();
            } else {
                return super.pd.getPropertyType();
            }
        }
    }
}
