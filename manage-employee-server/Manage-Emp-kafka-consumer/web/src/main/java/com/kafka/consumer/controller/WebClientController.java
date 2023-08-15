package com.kafka.consumer.controller;


import java.util.List;
import java.util.Objects;

import com.kafka.consumer.UserRepository;
import com.kafka.consumer.entity.JobDetails;
import com.kafka.consumer.entity.Message;
import com.kafka.consumer.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import javax.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/test")
@Slf4j
public class WebClientController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/getData/{userID}")
    public Message<?> getData(@PathVariable Long userID, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        WebClient webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.AUTHORIZATION, token)
                .build();

        String jobDetailsByUserApiUrl = "http://localhost:8080/api/cronjob/getJobByUser/{userID}";
        String userApiUrl = "http://localhost:8080/api/auth/getUserByID/{userID}";

        Mono<Message<User>> userByID = webClient.get()
                .uri(userApiUrl, userID)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Message<User>>() {
                });

        Flux<Message<List<JobDetails>>> jobDetailsByUser = userByID.flatMapMany(userMessage -> {
            User user = userMessage.getObject();
            return webClient.get()
                    .uri(jobDetailsByUserApiUrl, user.getUserID())
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Message<List<JobDetails>>>() {
                    });
        });

        jobDetailsByUser.subscribe(jobDetails -> {
            jobDetails.getObject().forEach(jobDetail -> log.info(jobDetail.toString()));
        });
        return new Message<>("Success", HttpStatus.OK, Objects.requireNonNull(jobDetailsByUser.blockLast()).getObject());
    }
}
