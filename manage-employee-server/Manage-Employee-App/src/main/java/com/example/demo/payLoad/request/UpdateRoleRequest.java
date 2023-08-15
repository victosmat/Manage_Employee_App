package com.example.demo.payLoad.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateRoleRequest {
    private Long userID;
    private List<String> roles;
}
