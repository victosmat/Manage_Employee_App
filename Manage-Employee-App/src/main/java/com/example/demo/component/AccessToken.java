package com.example.demo.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@RedisHash("Token")
public class AccessToken implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long userID;
    private String token;
}
