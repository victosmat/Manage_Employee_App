package com.kafka.consumer.service;

import com.kafka.consumer.payload.UserRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class UserEventConsumerWithManualAck implements AcknowledgingMessageListener<String, UserRequest> {
    @Override
    @KafkaListener(topics = {"add-user-in-session", "remove-user-from-session"})
    public void onMessage(ConsumerRecord<String, UserRequest> data, Acknowledgment acknowledgment) {
        log.info("Message Consumed = {}", data.toString());
        assert acknowledgment != null;
        acknowledgment.acknowledge();
        log.info("Message acknowledge");
    }
}
