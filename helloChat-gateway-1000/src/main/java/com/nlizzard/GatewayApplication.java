package com.nlizzard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@EnableDiscoveryClient // 可加可不加，新版cloud根据依赖和配置会自动注册
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(com.nlizzard.GatewayApplication.class,args);
    }
}
