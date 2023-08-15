package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatisticalErrorDTO   {
    private Long userID;
    private String checkInAt;
    private String checkOutAt;
    private String statusError;
}
