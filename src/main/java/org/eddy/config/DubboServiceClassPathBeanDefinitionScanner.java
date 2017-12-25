package org.eddy.config;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

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
                .addConstructorArgValue(service)
                .addPropertyReference("ref", beanDefinition.getBeanClassName())
                .addPropertyValue("interfaceClass", service.interfaceClass() == null ? beanClass.getInterfaces()[0] : service.interfaceClass());

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



    }

    private List<RuntimeBeanReference> beanReferences(String[] registryConfigBeanNames) {
        return Arrays.stream(registryConfigBeanNames).map(name -> {
            return new RuntimeBeanReference(name);
        }).collect(Collectors.toList());
    }
}
