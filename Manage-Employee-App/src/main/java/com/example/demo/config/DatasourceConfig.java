package com.example.demo.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@ConfigurationProperties("spring.datasource")
public class DatasourceConfig {

    private String driverClassName;
    private String url;

    @Bean
    @Profile("dev")
    public void devEnvironmentSetup() {
        System.out.println("=====================================");
        System.out.println("[DEV] - Environment");
        System.out.println(driverClassName);
        System.out.println(url);
        System.out.println("=====================================");
    }

    @Bean
    @Profile("prod")
    public void productionEnvironmentSetup() {
        System.out.println("=====================================");
        System.out.println("[PRODUCTION] - Environment");
        System.out.println(driverClassName);
        System.out.println(url);
        System.out.println("=====================================");
    }

}
