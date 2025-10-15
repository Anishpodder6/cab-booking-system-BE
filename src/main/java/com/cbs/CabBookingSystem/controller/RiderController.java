package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class RiderController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRegistrationDto userRegistrationDto){
//         log.info("hello ",userRegistrationDto);
        User registeredUser = userService.registerUser(userRegistrationDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

}
