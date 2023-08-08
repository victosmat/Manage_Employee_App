package com.kafka.consumer.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UserRequest {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String username;
}
