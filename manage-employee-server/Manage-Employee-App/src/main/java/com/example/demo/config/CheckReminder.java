package com.example.demo.config;

import com.example.demo.component.MailComponent;
import com.example.demo.entity.Structure;
import com.example.demo.repository.StructureRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@Slf4j
public class CheckReminder {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StructureRepository structureRepository;
    @Autowired
    private EmailService emailService;

    @Value("${spring.scheduled.minute}")
    private String minute;
    @Value("${spring.scheduled.hourCheckIn}")
    private String hourCheckIn;
    @Value("${spring.scheduled.hourCheckOut}")
    private String hourCheckOut;
    @Value("${spring.scheduled.day}")
    private String day;

    //    @Scheduled(cron = "0 0/30 8-12 * * ?")
    @Scheduled(cron = "0 ${spring.scheduled.minute} ${spring.scheduled.hourCheckIn} * * ${spring.scheduled.day}")
    public void sendCheckInReminder() {

        userRepository.findAll().forEach(user -> {
            LocalTime checkinTime = LocalTime.parse(user.getTime().getCheckIn());
            LocalTime now = LocalTime.now();
            List<Structure> structures = structureRepository.checkStructureInUser(String.valueOf(LocalDate.now()), user.getId());
            Structure structure = structures.get(0);
            if (now.isAfter(checkinTime) && structure == null) {
                String content = "you forgot to check-in with code " + user.getId();
                MailComponent mailDTO = new MailComponent(user.getEmail(), "Check-in notice!", content);
                emailService.sendMail(mailDTO);
            }
        });
    }

    @Scheduled(cron = "0 ${spring.scheduled.minute} ${spring.scheduled.hourCheckOut} * * ${spring.scheduled.day}")
    public void sendCheckOutReminder() {
        userRepository.findAll().forEach(user -> {
            List<Structure> structures = structureRepository.checkStructureInUser(String.valueOf(LocalDate.now()), user.getId());
            if (structures.size() == 1) {
                String content = "you forgot to check-out with code " + user.getId();
                MailComponent mailDTO = new MailComponent(user.getEmail(), "Check-out notice!", content);
                emailService.sendMail(mailDTO);
            }
        });
    }
}
