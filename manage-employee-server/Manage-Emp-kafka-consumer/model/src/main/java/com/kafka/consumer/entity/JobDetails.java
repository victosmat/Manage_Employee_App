package com.kafka.consumer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobDetails {
    private String jobCode;
    private String name;
    private String cronTime;
    private String date;
    private String description;
    private List<Long> userIDs;
}
