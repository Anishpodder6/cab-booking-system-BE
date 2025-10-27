package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.UserLoginDto;
import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.dto.UserUpdateDto;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
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
    public ResponseEntity<?> getUserProfileById(@PathVariable Long userId) {
        User user = null;

        try {
          user = userService.findUserById(userId);
            if (user != null) {
                user.setPasswordHash(null);     //SCRUM-222 : Exclusion of sensitive data
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateUserProfileById(@RequestBody @Valid UserUpdateDto userUpdateDto, @PathVariable Long userId) {

        try {
            User updatedUser = userService.updateUserProfileById(userUpdateDto, userId);
            if (updatedUser == null) {
                return new ResponseEntity<>("User with ID " + userId + " not found.", HttpStatus.NOT_FOUND);
            }
            updatedUser.setPasswordHash(null);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/profile/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long userId){
        User getUser = null;

        try {
            getUser = userService.findUserById(userId);

            if (getUser != null) {
                userService.deleteUserById(userId);
            }
            return new ResponseEntity<>("User deleted succesfully", HttpStatus.OK);

        } catch (ResourceNotFoundException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

//    @GetMapping("/allDetails/{userId}")
//    public ResponseEntity<>

}
