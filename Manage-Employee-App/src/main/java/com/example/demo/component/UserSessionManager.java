package com.example.demo.component;

import com.example.demo.entity.User;
import com.example.demo.payLoad.mapper.MapperRequestToDTO;
import com.example.demo.payLoad.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("singleton")
public class UserSessionManager {
    private final Map<Long, UserDTO> activeSessions;

    @Autowired
    private MapperRequestToDTO mapperRequestToDTO;

    public UserSessionManager() {
        activeSessions = new HashMap<>();
    }

    public void createSession(User user) {
        activeSessions.put(user.getID(), mapperRequestToDTO.mapperUserToDTO(user));
    }

    public List<UserDTO> getAllSession() {
        List<UserDTO> userDTOList = new ArrayList<>();
        return activeSessions.values().stream().toList();
    }

    public void invalidateSession(Long userId) {
        activeSessions.remove(userId);
    }
}

