package com.example.demo.payLoad.dto;

import com.example.demo.payLoad.request.AddressRequest;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public interface IUserDTO {
    @Value("#{target.ID}")
    Long getUserID();

    @Value("#{target.fullName}")
    String getFullName();

    @Value("#{target.email}")
    String getEmail();

    @Value("#{target.account.username}")
    String getUsername();
}
