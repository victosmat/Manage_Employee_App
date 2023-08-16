package com.example.demo;

import com.example.demo.controller.AuthController;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindByUserName() {
        User user = new User("Le Tuan Minh", null, "letuanminh@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        Mockito.when(userRepository.findByEmail("letuanminh@gmail.com")).thenReturn(user);
        User resultUser = userRepository.findByEmail("letuanminh@gmail.com");
        assertEquals(user, resultUser);
    }

    @Test
    public void testFindAll() {
        List<User> users = new ArrayList<>();
        User user1 = new User("Le Tuan Minh 1", null, "letuanminh1@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        User user2 = new User("Le Tuan Minh 2", null, "letuanminh2@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        User user3 = new User("Le Tuan Minh 3", null, "letuanminh3@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        users = Arrays.asList(user1, user2, user3);
        Mockito.when(userService.findAll()).thenReturn(users);
        List<User> empList = userService.findAll();
        assertEquals(users.size(), empList.size());
    }

    @Test
    void getAllUserTest() throws Exception {
        User user1 = new User("Le Tuan Minh 1", null, "letuanminh1@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        User user2 = new User("Le Tuan Minh 2", null, "letuanminh2@gmail.com", null, User.AuthProvider.LOCAL, null, null);
        final List<User> userList = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(userList);
        List<User> users = authController.getUser();
        assertThat(users).containsExactly(user1, user2);
    }
}
