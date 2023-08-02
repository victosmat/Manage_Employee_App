package com.example.demo.payLoad.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserRegistryRequest {
    private String fullName;
    private List<AddressRequest> address;
    private String email;
    private String username;
    private String password;
}
