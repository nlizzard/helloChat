package com.nlizzard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

@Configuration
public class StaticResourceConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
//        //搭配文件存储到本地方案一使用
//        registry.addResourceHandler("/static/**")
//                .addResourceLocations("file:D:/program/");
        super.addResourceHandlers(registry);
    }
}
