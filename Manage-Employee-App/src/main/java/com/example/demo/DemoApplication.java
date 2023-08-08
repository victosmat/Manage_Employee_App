package com.example.demo;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
//        DatabaseConnector databaseConnector = (DatabaseConnector) context.getBean("mysqlConnector");
//        databaseConnector.connect();
    }

    @Bean
    NewTopic addUserToSession() {
        return new NewTopic("add-user-in-session", 2, (short) 1);
    }

    @Bean
    NewTopic removeUserFromSession() {
        return new NewTopic("remove-user-in-session", 1, (short) 1);
    }
}