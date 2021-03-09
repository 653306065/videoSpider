package com.spider.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContentUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        SpringContentUtil.applicationContext = applicationContext;
    }

    public static <T> T getBean(Class<T> t) {
        return applicationContext.getBean(t);
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
