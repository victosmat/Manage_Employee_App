package com.example.demo.payLoad.connect;

public class MySqlConnector extends DatabaseConnector {
    @Override
    public void connect() {
        System.out.println("Connected to Mysql: " + getUrl());
    }
}
