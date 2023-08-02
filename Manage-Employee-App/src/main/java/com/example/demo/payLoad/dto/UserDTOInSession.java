package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTOInSession {
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String username;
}
