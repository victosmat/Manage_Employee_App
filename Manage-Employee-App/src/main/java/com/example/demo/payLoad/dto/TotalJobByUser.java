package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalJobByUser {
    private Long userID;
    private Long totalJob;
}
