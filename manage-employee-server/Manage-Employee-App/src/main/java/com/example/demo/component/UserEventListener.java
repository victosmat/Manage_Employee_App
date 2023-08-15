package com.example.demo.component;

import com.example.demo.entity.User;
import com.example.demo.payLoad.dto.UserDTOInSession;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class UserEventListener  {
    @Autowired
    private MapperRequestToDTO mapperRequestToDTO;
    @Autowired
    private KafkaTemplate<String, UserDTOInSession> kafkaTemplate;

    @EventListener
    public void handleUserLoginEvent(UserLoginEvent event) {
        UserDTOInSession userDTOInSession = event.getUserDTOInSession();
        kafkaTemplate.send("add-user-in-session", userDTOInSession)
                .addCallback(new ListenableFutureCallback<SendResult<String, UserDTOInSession>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.info("Unable to send message=[{}] due to : {}", userDTOInSession.toString(), ex.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<String, UserDTOInSession> result) {
                        log.info("Sent message=[{}] with offset=[{}]", userDTOInSession.toString(), result.getRecordMetadata().offset());
                    }
                });
    }

    @EventListener
    public void handleUserLogoutEvent(UserLogoutEvent event){
        UserDTOInSession userDTOInSession = event.getUserDTOInSession();
        kafkaTemplate.send("remove-user-in-session", userDTOInSession)
                .addCallback(new ListenableFutureCallback<SendResult<String, UserDTOInSession>>() {
                    @Override
                    public void onFailure(Throwable ex) {
                        log.info("Unable to send message=[{}] due to : {}", userDTOInSession.toString(), ex.getMessage());
                    }

                    @Override
                    public void onSuccess(SendResult<String, UserDTOInSession> result) {
                        log.info("Sent message=[{}] with offset=[{}]", userDTOInSession.toString(), result.getRecordMetadata().offset());
                    }
                });
    }
}
