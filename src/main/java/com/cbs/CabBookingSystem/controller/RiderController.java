package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.UserLoginDto;
import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class RiderController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@Valid @RequestBody UserRegistrationDto userRegistrationDto){
        User registeredUser = userService.registerUser(userRegistrationDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginDto loginDto) {
        try {
            User user = userService.findUserByEmail(loginDto.getEmail());
            if(user.getPasswordHash().equals(loginDto.getPasswordHash())) {
                return ResponseEntity.ok("Login successful for user: " + user.getFirstName());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfileById(@PathVariable Long userId){
        User user = userService.getUserProfileById(userId);

        if(user != null) {
            user.setPasswordHash(null);     //SCRUM-222 : Exclusion of sensitive data
            return new ResponseEntity<>(user, HttpStatus.OK);
        }

        return new ResponseEntity<>("User not found with ID : " + userId,HttpStatus.NOT_FOUND);
    }

}
