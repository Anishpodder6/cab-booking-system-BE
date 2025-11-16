package com.cbs.vector.controller;


import com.cbs.vector.dto.*;
// NEW IMPORT
import com.cbs.vector.exception.ResourceNotFoundException;
import com.cbs.vector.model.Driver;
import com.cbs.vector.model.DriverStatus;
import com.cbs.vector.model.RideWithRating;
import com.cbs.vector.service.DriverService;
import com.cbs.vector.service.RideService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }


    // 1. GET /api/drivers/available
    @GetMapping("/available")
    public List<DriverResponseDTO> getAvailableDrivers() {
        log.info("Request to fetch all available drivers.");
        return driverService.getAvailableDrivers();
    }

    // 2. PUT /api/drivers/status/{id}
    @PutMapping("/status/{id}")
    public ResponseEntity<DriverResponseDTO> updateDriverStatus(@PathVariable UUID id, @RequestBody DriverStatusUpdateRequest request) {
        try {
            DriverStatus newStatus = DriverStatus.valueOf(request.getStatus().toUpperCase());

            log.info("Successfully updated driver ID {} status to: {}", id, newStatus);
            return driverService.updateDriverStatus(id, newStatus)
                    .map(updatedDriverDTO -> new ResponseEntity<>(updatedDriverDTO, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (IllegalArgumentException e) {
            log.error("Invalid status value '{}' provided for driver ID {}.", request.getStatus(), id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Inner class for PUT request body (remains the same)
    @Getter
    private static class DriverStatusUpdateRequest {
        private String status;

        public void setStatus(String status) {
            this.status = status;
        }
    }


    //PUT Method in the Driver profile Section
    @PutMapping("/profile/{userId}")
    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable UUID userId, @RequestBody DriverUpdateDTO updateDTO) {
        log.info("dto from controller " + updateDTO);
        log.info("Request to update driver profile for ID: {}", userId);
        DriverResponseDTO updated = driverService.updateDriverProfile(userId, updateDTO);
        if (updated == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    //GET Method in the Driver profile Section
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getDriverAllDetails(@PathVariable UUID userId) {
            DriverResponseDTO user = null;

            try {
                user = driverService.findUserById(userId);
                if (user != null) {
                    log.info("Successfully fetched driver profile for user ID: {}", userId);
                    return new ResponseEntity<>(user, HttpStatus.OK);
                }
            } catch (ResourceNotFoundException e) {
                log.error("Resource not found for driver user ID {}: {}", userId, e.getMessage());
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            log.warn("Driver user profile for ID {} returned null unexpectedly.", userId);
            return new ResponseEntity<>(user, HttpStatus.OK);
    }

    //Driver History

    @GetMapping("/driverHistory/search")
    public ResponseEntity<List<Driver>> searchDriverRideHistory(@RequestParam String keyword){
        List<Driver> products = driverService.searchDriverRideHistory(keyword);
        log.info("Found {} results for keyword: {}", products.size(), keyword);
        System.out.println("searching with "+ keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
    @Autowired
    private RideService rideService;

    @GetMapping("/history/{driverId}")
    public ResponseEntity<List<RideWithRating>> getDriverRideHistory(@PathVariable UUID driverId) {
        List<RideWithRating> history = rideService.getDriverHistory(driverId);
        log.info("Fetched {} rides in history for driver ID: {}", history.size(), driverId);
        return ResponseEntity.ok(history);
    }

    // Driver Dashboard Data
    @GetMapping("/dashboard/{driverId}")
    public ResponseEntity<DriverAllDetailsResponseDTO> getDriverDashboard(@PathVariable UUID driverId) {

        DriverAllDetailsResponseDTO dashboardData = driverService.getDriverDashboardData(driverId);

        if (dashboardData.driverId() == null) {
            log.warn("Dashboard data could not be fetched (Driver ID {} not found).", driverId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        log.info("Successfully fetched dashboard data for driver ID: {}", driverId);
        return new ResponseEntity<>(dashboardData, HttpStatus.OK);
    }

}