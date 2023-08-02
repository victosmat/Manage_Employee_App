package com.example.demo.component;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public enum TokenValidationStatus {
    VALID,
    INVALID_SIGNATURE,
    INVALID_TOKEN,
    TOKEN_EXPIRED,
    UNSUPPORTED_TOKEN,
    EMPTY_CLAIMS
}
