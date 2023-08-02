package com.example.demo.payLoad.dto;

import com.example.demo.entity.Structure;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StructureDTO {
    private Long userID;
    private String status;
    private String dateTime;
}
