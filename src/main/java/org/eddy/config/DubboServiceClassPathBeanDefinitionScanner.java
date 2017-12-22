package org.eddy.config;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

public class DubboServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
    public DubboServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public void registerFilter() {

    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        return super.doScan(basePackages);
    }
}
