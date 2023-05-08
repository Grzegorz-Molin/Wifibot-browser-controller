package com.company.proxy.util;

import com.company.proxy.CustomContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public CustomContextAware customContextAware() {
        return new CustomContextAware();
    }
}

