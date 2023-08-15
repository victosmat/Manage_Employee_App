package com.example.demo.controller;

import com.example.demo.component.UserSessionManager;
import com.example.demo.entity.CheckTimeRequest;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.*;
import com.example.demo.payLoad.request.TimeFromToRequest;
import com.example.demo.payLoad.request.UpdateRoleRequest;
import com.example.demo.service.StructureService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/admin")
@Slf4j
public class AdminController {
    @Autowired
    private StructureService structureService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSessionManager userSessionManager;

    @GetMapping("/getAllUsersInSession")
    public Message<List<UserDTO>> getAllUsersInSession() {
        return new Message<>("get all users in session successfully!", HttpStatus.OK, userSessionManager.getAllSession());
    }

    @GetMapping("/getAllUsers")
    public Message<List<IUserDTO>> getAllUsers() {
        return userService.getAllUsers();
    }

    @PutMapping("/updatePosition")
    public Message<UserDTO> updatePosition(@RequestBody UpdateRoleRequest updateRoleRequest) {
        return userService.updateRole(updateRoleRequest);
    }

    @GetMapping("/getAllStructures")
    public Message<List<IStatisticalNorDTO>> getAllStructures(@RequestBody TimeFromToRequest timeFromToRequest) {
        return structureService.getAllStructures(timeFromToRequest.getFrom(), timeFromToRequest.getTo());
    }

    @GetMapping("/getAllStructuresError/{month}/{year}")
    public Message<List<StatisticalErrorDTO>> getAllStructuresError(@PathVariable int month, @PathVariable int year) {
        return structureService.getAllStructuresError(month, year);
    }

    @GetMapping("/getAllStructuresErrorByUser/{month}/{year}/{userID}")
    public Message<List<StatisticalErrorDTO>> getAllStructuresErrorByUser(@PathVariable int month, @PathVariable int year, @PathVariable Long userID) {
        return structureService.getAllStructuresErrorByUser(month, year, userID);
    }

    @PutMapping("/updateCheckTime/{userID}")
    public Message<CheckTimeRequest> updateTime(@PathVariable Long userID) {
        return structureService.updateCheckTime(userID);
    }

    @GetMapping("/getTotalJobByUser")
    public Message<List<TotalJobByUser>> getTotalJobByUser() {
        return userService.getTotalJobByUser();
    }

    @DeleteMapping("/deleteUser/{userId}")
    public Message<?> deleteUser(@PathVariable Long userId) {
        return userService.deleteUser(userId);
    }

    @GetMapping("/getAllUsersByPage/{page}/{size}")
    public Message<Page<UserDTO>> getAllUsersByPage(@PathVariable int page, @PathVariable int size) {
        return userService.getAllUsersByPage(page, size);
    }

    @GetMapping("/getAllUsersBySlice/{page}/{size}")
    public Message<Slice<UserDTO>> getAllUsersBySlice(@PathVariable int page, @PathVariable int size) {
        return userService.getAllUsersBySlice(page, size);
    }
}
