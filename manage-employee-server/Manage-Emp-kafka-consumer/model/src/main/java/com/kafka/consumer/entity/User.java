package com.kafka.consumer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long userID;
    private String fullName;
    private List<Address> address;
    private String email;
    private String role;
    private String username;
}
