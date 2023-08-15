package com.example.demo.component;

import com.example.demo.payLoad.dto.UserDTOInSession;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserLogoutEvent extends ApplicationEvent {
    private final UserDTOInSession userDTOInSession;

    public UserLogoutEvent(Object source, UserDTOInSession userDTOInSession) {
        super(source);
        this.userDTOInSession = userDTOInSession;
    }
}
