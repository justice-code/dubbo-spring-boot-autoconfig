package org.eddy.config;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

public class DubboServiceClassPathBeanDefinitionScanner extends ClassPathBeanDefinitionScanner {
    public DubboServiceClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public void registerFilter() {

    }
}
