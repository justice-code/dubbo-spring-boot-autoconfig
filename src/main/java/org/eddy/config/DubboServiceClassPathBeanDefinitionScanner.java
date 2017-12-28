package org.eddy.config;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DubboServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {

    public DubboServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public void registerFilter() {
        addIncludeFilter(new AnnotationTypeFilter(Service.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitionHolderSet = super.doScan(basePackages);

        beanDefinitionHolderSet.stream().forEach(beanDefinitionHolder -> {
            registerServiceBean(beanDefinitionHolder, getRegistry());
        });

        return beanDefinitionHolderSet;
    }

    private void registerServiceBean(BeanDefinitionHolder beanDefinitionHolder, BeanDefinitionRegistry registry) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();

        Class<?> beanClass = ClassUtils.resolveClassName(beanDefinition.getBeanClassName(), DubboServiceClassPathBeanDefinitionScanner.class.getClassLoader());
        Service service = beanClass.getAnnotation(Service.class);

        BeanDefinitionBuilder serverBuilder = BeanDefinitionBuilder.rootBeanDefinition(ServiceBean.class)
                .setRole(BeanDefinition.ROLE_APPLICATION)
                .addConstructorArgValue(service)
                .addPropertyReference("ref", beanDefinitionHolder.getBeanName())
                .addPropertyValue("interfaceClass", service.interfaceClass() == void.class ? beanClass.getInterfaces()[0] : service.interfaceClass());

        String[] registryConfigBeanNames = service.registry();
        List<RuntimeBeanReference> registryRuntimeBeanReferences = beanReferences(registryConfigBeanNames);
        if (! CollectionUtils.isEmpty(registryRuntimeBeanReferences)) {
            serverBuilder.addPropertyValue("registries", registryRuntimeBeanReferences);
        }

        String[] protocols = service.protocol();
        List<RuntimeBeanReference> protocolRuntimeBeanReferences = beanReferences(protocols);
        if (! CollectionUtils.isEmpty(protocolRuntimeBeanReferences)) {
            serverBuilder.addPropertyValue("protocols", protocolRuntimeBeanReferences);
        }

        String[] keyValues = service.parameters();
        ManagedMap parameters = buildParameters(keyValues);
        if (null != parameters) {
            serverBuilder.addPropertyValue("parameters", parameters);
        }

        if (! StringUtils.isEmpty(service.module())) {
            serverBuilder.addPropertyReference("module", service.module());
        }

        AbstractBeanDefinition serviceBean = serverBuilder.getBeanDefinition();
        registry.registerBeanDefinition(genBeanName(serviceBean), serviceBean);
    }

    private String genBeanName(AbstractBeanDefinition serviceBean) {
        return serviceBean.getBeanClassName() + "$service";
    }

    private ManagedMap buildParameters(String[] keyValues) {
        if (keyValues.length == 0 || keyValues.length % 2 != 0) {
            return null;
        }

        ManagedMap managedMap = new ManagedMap();
        for (int i = 0; i < keyValues.length; i+=2) {
            managedMap.put(keyValues[i], new TypedStringValue(keyValues[i + 1], String.class));
        }

        return managedMap;
    }

    private List<RuntimeBeanReference> beanReferences(String[] registryConfigBeanNames) {
        return Arrays.stream(registryConfigBeanNames).map(name -> {
            return new RuntimeBeanReference(name);
        }).collect(Collectors.toList());
    }
}
