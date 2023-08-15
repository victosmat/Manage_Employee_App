package com.kafka.consumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.consumer.UserRepository;
import com.kafka.consumer.entity.UserInSession;
import com.kafka.consumer.payload.UserRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserEventConsumer {
    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(id = "${spring.kafka.consumer.group-id}", topics = {"add-user-in-session"})
    public void addUserInSession(ConsumerRecord<String, String> event) throws Exception {
        log.info("Event Received = {}", event);
        String payload = event.value();
        try {
            UserRequest userRequest = objectMapper.readValue(payload, UserRequest.class);
            if (userRepository.existsByUsername(userRequest.getUsername())) {
                log.info("User already exists in session: {}", userRequest);
                return;
            }
            userRepository.save(mapUserRequestToSession(userRequest));
            log.info("User added to session: {}", userRequest);
        } catch (Exception e) {
            log.error("Failed to parse JSON payload: {}", payload, e);
        }
    }

    @KafkaListener(topics = {"remove-user-in-session"})
    public void removeUserFromSession(ConsumerRecord<String, String> event) throws Exception {
        log.info("Event Received = {}", event);
        String payload = event.value();
        try {
            UserRequest userRequest = objectMapper.readValue(payload, UserRequest.class);
            userRepository.deleteById(userRequest.getUserId());
            log.info("User removed from session: {}", userRequest);
        } catch (Exception e) {
            log.error("Failed to parse JSON payload: {}", payload, e);
        }
    }

    public UserInSession mapUserRequestToSession(UserRequest userRequest) {
        UserInSession userInSession = new UserInSession();
        userInSession.setUserId(userRequest.getUserId());
        userInSession.setFullName(userRequest.getFullName());
        userInSession.setEmail(userRequest.getEmail());
        userInSession.setRole(userRequest.getRole());
        userInSession.setUsername(userRequest.getUsername());
        return userInSession;
    }
}
