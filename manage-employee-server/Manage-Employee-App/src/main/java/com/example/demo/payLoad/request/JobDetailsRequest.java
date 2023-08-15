package com.example.demo.payLoad.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetailsRequest {
    private String jobCode;
    private String name;
    private String cronTime;
    private String date;
    private String description;
    private List<Long> userIDs;
}
