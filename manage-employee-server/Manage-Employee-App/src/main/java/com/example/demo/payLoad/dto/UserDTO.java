package com.example.demo.payLoad.dto;

import com.example.demo.payLoad.request.AddressRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO implements Serializable {
    private Long userID;
    private String fullName;
    private List<AddressRequest> address;
    private String email;
    private String role;
    private String username;

    @Override
    public String toString() {
        return "UserDTO{" +
                "userID=" + userID +
                ", fullName='" + fullName + '\'' +
                ", address=" + address +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
