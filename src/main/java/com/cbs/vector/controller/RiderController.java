package com.cbs.vector.controller;

import com.cbs.vector.dto.*;
import com.cbs.vector.exception.ResourceNotFoundException;
import com.cbs.vector.model.RideWithRating;
import com.cbs.vector.service.RideService;
import com.cbs.vector.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/riders")
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class RiderController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfileById(@PathVariable UUID userId) {
        log.info("Attempting to fetch rider profile for ID: {}", userId);
        RiderRegistrationResponseDTO user = null;
        log.info("Profile  : {}" , user + " UserId: {}" , userId);
        try {
          user = userService.findUserById(userId);
            if (user != null) {
                log.info("Successfully fetched rider profile for ID: {}", userId);
                return new ResponseEntity<>(user, HttpStatus.OK);

            }
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found for rider ID {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateUserProfileById(@RequestBody @Valid UserUpdateDto userUpdateDto, @PathVariable UUID userId) {

        try {
            RiderRegistrationResponseDTO updatedUser = userService.updateUserProfileById(userUpdateDto, userId);
            if (updatedUser == null) {
                log.warn("Update failed: User with ID {} not found by service.", userId);
                return new ResponseEntity<>("User with ID " + userId + " not found.", HttpStatus.NOT_FOUND);
            }
            log.info("Successfully updated rider profile for ID: {}", userId);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found while updating rider ID {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/profile/{userId}")
    public ResponseEntity<String> deleteUserById(@PathVariable UUID userId){
        RiderRegistrationResponseDTO getUser = null;

        try {
            getUser = userService.findUserById(userId);

            if (getUser != null) {
                userService.deleteUserById(userId);
            }
            log.info("Successfully deleted user profile for ID: {}", userId);
            return new ResponseEntity<>("User deleted succesfully", HttpStatus.OK);

        } catch (ResourceNotFoundException e){
            log.error("Resource not found while deleting rider ID {}: {}", userId, e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("/allDetails/{userId}")
    public ResponseEntity<RiderAllDetailsResponseDTO> getRiderDetailsReport(@PathVariable UUID userId) {
        RiderAllDetailsResponseDTO detailsResponseDTO = userService.getRiderAllDetails(userId);
        log.info("Attempting to get all details report for ID: {}", userId);
        return ResponseEntity.ok(detailsResponseDTO);
    }

    //Rider Ride History

    @Autowired
    private RideService rideService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<RideWithRating>> getRiderRideHistory(@PathVariable UUID userId) {

        List<RideWithRating> history = rideService.getRiderHistory(userId);
        log.info("Fetched {} rides for rider ID: {}", history.size(), userId);
        return ResponseEntity.ok(history);
    }

}
