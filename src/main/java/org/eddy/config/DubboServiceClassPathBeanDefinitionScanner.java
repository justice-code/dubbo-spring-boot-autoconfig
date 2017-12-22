package org.eddy.config;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.Set;

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
    }
}
