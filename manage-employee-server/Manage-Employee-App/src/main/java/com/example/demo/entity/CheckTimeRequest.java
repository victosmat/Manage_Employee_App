package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "Check_time_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckTimeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;
    @Column(name = "user_id")
    private Long userID;
    private String checkIn;
    private String checkOut;
    private boolean isAccept;
}