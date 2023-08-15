package com.example.demo.controller;

import com.example.demo.payLoad.Message;
import com.example.demo.payLoad.dto.StructureDTO;
import com.example.demo.service.StructureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/user")
@Slf4j
public class UserController {
    @Autowired
    private StructureService structureService;

    @PostMapping("/checkTime/{userID}")
    @Profile("dev")
    public Message<StructureDTO> checkTime(@PathVariable Long userID) {
        return structureService.checkTime(userID);
    }
}
