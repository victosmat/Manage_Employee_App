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
    public void initializationStructure() {
        String localDateTime = String.valueOf(LocalDateTime.now());
        userRepository.findAll().forEach(user -> {
            Structure structure = new Structure(null, user, Structure.Status.CHECK_IN_MISSING, localDateTime);
            structureRepository.save(structure);
        });
    }
}
