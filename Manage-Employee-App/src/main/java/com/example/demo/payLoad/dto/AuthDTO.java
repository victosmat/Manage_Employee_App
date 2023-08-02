package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthDTO {
    private Long userID;
    private String username;
    private String token;
}
