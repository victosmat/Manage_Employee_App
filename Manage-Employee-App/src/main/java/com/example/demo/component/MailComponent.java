package com.example.demo.component;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailComponent {
    private String to;
    private String subject;
    private String content;
}
