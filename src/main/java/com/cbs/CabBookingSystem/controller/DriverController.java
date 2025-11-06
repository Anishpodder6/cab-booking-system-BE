package com.cbs.CabBookingSystem.controller;


import com.cbs.CabBookingSystem.dto.*;
// NEW IMPORT
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.Driver;
import com.cbs.CabBookingSystem.model.DriverStatus;
import com.cbs.CabBookingSystem.model.RideWithRating;
import com.cbs.CabBookingSystem.service.DriverService;
import com.cbs.CabBookingSystem.service.RideService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class DriverController {

    @Autowired
    private DriverService driverService;


    // 1. GET /api/drivers/available (EXISTING)
    @GetMapping("/available")
    public List<DriverResponseDTO> getAvailableDrivers() {
        return driverService.getAvailableDrivers();
    }

    // 2. PUT /api/drivers/status/{id} (EXISTING)
    @PutMapping("/status/{id}")
    public ResponseEntity<DriverResponseDTO> updateDriverStatus(@PathVariable UUID id, @RequestBody DriverStatusUpdateRequest request) {
        try {
            DriverStatus newStatus = DriverStatus.valueOf(request.getStatus().toUpperCase());

            return driverService.updateDriverStatus(id, newStatus)
                    .map(updatedDriverDTO -> new ResponseEntity<>(updatedDriverDTO, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Inner class for PUT request body (remains the same)
    @Getter
    private static class DriverStatusUpdateRequest {
        private String status;

        public void setStatus(String status) { this.status = status; }
    }


    //PUT Method in the Driver profile Section
    @PutMapping("/{id}")
    public ResponseEntity<DriverResponseDTO> updateDriver(@PathVariable UUID id, @RequestBody DriverUpdateDTO updateDTO) {
        // The service layer handles finding, updating, and returning the DTO
        return driverService.updateDriverProfile(id, updateDTO)
                .map(updatedDriverDTO -> new ResponseEntity<>(updatedDriverDTO, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }




    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getDriverAllDetails(@PathVariable UUID userId) {
            DriverResponseDTO user = null;

            try {
                user = driverService.findUserById(userId);
                if (user != null) {
//                user.setPasswordHash(null);     //SCRUM-222 : Exclusion of sensitive data
                    return new ResponseEntity<>(user, HttpStatus.OK);
                }
            } catch (ResourceNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(user, HttpStatus.OK);
    }

    //Driver History

    //Search Rides from history
    @GetMapping("/driverHistory/search")
    public ResponseEntity<List<Driver>> searchDriverRideHistory(@RequestParam String keyword){
        List<Driver> products = driverService.searchDriverRideHistory(keyword);
        System.out.println("searching with "+ keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }
    @Autowired
    private RideService rideService;

    //Driver Ride History

    @GetMapping("/history/{driverId}")
    public ResponseEntity<List<RideWithRating>> getDriverRideHistory(@PathVariable UUID driverId) {

//        UUID driverId = getDriverIdFromDetails(driverId); // Retrieve authenticated driver's ID
        List<RideWithRating> history = rideService.getDriverHistory(driverId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/dashboard/{driverId}")
    public ResponseEntity<DriverAllDetailsResponseDTO> getDriverDashboard(@PathVariable UUID driverId) {

        DriverAllDetailsResponseDTO dashboardData = driverService.getDriverDashboardData(driverId);

        if (dashboardData.driverId() == null) {
            // This is a weak check; a better check is in the service using findById.
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dashboardData, HttpStatus.OK);
    }

}