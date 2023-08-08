package com.kafka.consumer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message<T> {
    private String message;
    private HttpStatus httpStatus;
    private T object;
}
