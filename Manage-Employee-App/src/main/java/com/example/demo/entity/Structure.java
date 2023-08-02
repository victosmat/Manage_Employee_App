package com.example.demo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Structure")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Structure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        CHECK_IN, CHECK_IN_LATE, CHECK_OUT, CHECK_OUT_EARLY, CHECK_IN_MISSING
    }

    private String dateTime;
}
