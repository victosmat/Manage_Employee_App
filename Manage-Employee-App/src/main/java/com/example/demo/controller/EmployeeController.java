package com.example.demo.controller;

import com.example.demo.entity.CheckTimeRequest;
import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.IStatisticalNorDTO;
import com.example.demo.payLoad.request.TimeFromToRequest;
import com.example.demo.payLoad.request.TimeRequest;
import com.example.demo.service.StructureService;
import com.example.demo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/employee")
@Slf4j
public class EmployeeController {
    @Autowired
    private StructureService structureService;
    @Autowired
    private UserService userService;

    @GetMapping("/getAllStructuresByUser")
    public Message<List<IStatisticalNorDTO>> getAllStructuresByUser(HttpServletRequest httpServletRequest, @RequestBody TimeFromToRequest timeFromToRequest) {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        return structureService.getAllStructuresByUser(timeFromToRequest.getFrom(), timeFromToRequest.getTo(), token);
    }

    @PutMapping("/updateTime")
    public Message<CheckTimeRequest> updateTime(HttpServletRequest httpServletRequest, @RequestBody TimeRequest timeRequest) {
        String token = httpServletRequest.getHeader("Authorization").substring(7);
        return userService.updateTime(timeRequest, token);
    }
}
