package org.eddy.config;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ModuleConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DubboReferenceInjector implements ApplicationContextAware{

    private ApplicationContext applicationContext;

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
}
