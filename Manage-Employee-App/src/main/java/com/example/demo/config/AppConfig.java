package com.example.demo.config;

import com.example.demo.payLoad.connect.DatabaseConnector;
import com.example.demo.payLoad.connect.MySqlConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean("mysqlConnector")
    DatabaseConnector mysqlConfigure() {
        DatabaseConnector mySqlConnector = new MySqlConnector();
        mySqlConnector.setUrl("jdbc:mysql://localhost:3306/Manage_Employee_App");
        return mySqlConnector;
    }
}
