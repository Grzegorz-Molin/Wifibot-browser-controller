package com.company.proxy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class CustomContextAware implements ApplicationContextAware {
    private static ApplicationContext context;

    public CustomContextAware() {
        System.out.println("CustomContextAware instance created");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        context = applicationContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }
}

