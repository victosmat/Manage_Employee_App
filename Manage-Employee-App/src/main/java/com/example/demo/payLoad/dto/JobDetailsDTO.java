package com.example.demo.payLoad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailsDTO {
    private String jobCode;
    private String name;
    private String cronTime;
    private String date;
    private String description;
    private List<Long> userIDs;
}
