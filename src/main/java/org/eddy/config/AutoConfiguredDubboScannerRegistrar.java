package org.eddy.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

public class AutoConfiguredDubboScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        List<String> packages = AutoConfigurationPackages.get(beanFactory);

        registerDubboService(packages, registry);

        registerDubboReference(registry);
    }

    private void registerDubboReference(BeanDefinitionRegistry registry) {
        registry.registerBeanDefinition("dubboReferenceInjector", BeanDefinitionBuilder.rootBeanDefinition(DubboReferenceInjector.class).setRole(BeanDefinition.ROLE_INFRASTRUCTURE).getBeanDefinition());
    }

    private void registerDubboService(List<String> packages, BeanDefinitionRegistry registry) {
        DubboServiceClassPathBeanDefinitionScanner scanner = new DubboServiceClassPathBeanDefinitionScanner(registry);
        scanner.registerFilter();
        scanner.doScan(packages.toArray(new String[]{}));
    }
}
