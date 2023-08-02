package com.example.demo.payLoad.dto;

import com.example.demo.payLoad.request.AddressRequest;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public interface IUserDTO {
    @Value("#{target.ID}")
    Long getUserID();

    @Value("#{target.fullName}")
    String getFullName();

//    @Value("#{target.address" +
//            ".stream()" +
//            ".map(address -> new " +
//            "com.example.demo.payLoad.request.AddressRequest(" +
//            "address.getStreet(), " +
//            "address.getCity()))" +
//            ".collect(java.util.stream.Collectors.toList())}")
//    List<AddressRequest> getAddress();

    @Value("#{target.email}")
    String getEmail();

//    @Value("#{target.role" +
//            ".stream()" +
//            ".map(role -> role.getRoleName())" +
//            ".collect(java.util.stream.Collectors.joining(\", \"))}")
//    String getRole();

    @Value("#{target.account.username}")
    String getUsername();
}
