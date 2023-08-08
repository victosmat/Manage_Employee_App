package com.kafka.consumer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "User_In_Session")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    private Long userId;
    private String fullName;
    private String email;
    private String role;
    private String username;
}
