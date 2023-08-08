package com.example.demo.config;

import com.example.demo.entity.Structure;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InitializationStructure {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StructureRepository structureRepository;
    @Value("${spring.scheduled.hour.structure}")
    private int hourStructure;

        @Scheduled(cron = "0 * ${spring.scheduled.hour.structure} * * ${spring.scheduled.day}")
//    @Scheduled(cron = "0 38 9 * * *")
    public void initializationStructure() {
        userRepository.findAll().forEach(user -> {
            Structure structure = new Structure(null, user, Structure.Status.CHECK_IN_MISSING, String.valueOf(LocalDateTime.now()));
            structureRepository.save(structure);
        });
    }
}
