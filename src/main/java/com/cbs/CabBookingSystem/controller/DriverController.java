package com.cbs.CabBookingSystem.controller;



import com.cbs.CabBookingSystem.dto.DriverLoginDTO;
import com.cbs.CabBookingSystem.dto.DriverRegistrationDTO;
import com.cbs.CabBookingSystem.dto.DriverResponseDTO;
// NEW IMPORT
import com.cbs.CabBookingSystem.model.DriverStatus;
import com.cbs.CabBookingSystem.service.DriverService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    @Autowired
    private DriverService driverService;

    // 1. POST /api/drivers/register (EXISTING)
    @PostMapping("/register")
    public ResponseEntity<DriverResponseDTO> registerDriver(@RequestBody DriverRegistrationDTO registrationDTO) {
        DriverResponseDTO responseDTO = driverService.registerDriver(registrationDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // 2. POST /api/drivers/login (NEW)
    @PostMapping("/login")
    public ResponseEntity<DriverResponseDTO> loginDriver(@RequestBody DriverLoginDTO driverLoginDTO) {
        // Service attempts login and returns an Optional DTO
        return driverService.loginDriver(driverLoginDTO)
                // If successful, return 200 OK with driver details
                .map(driverDTO -> new ResponseEntity<>(driverDTO, HttpStatus.OK))
                // If failed (email not found or password mismatch), return 401 Unauthorized
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    // 3. GET /api/drivers/available (EXISTING)
    @GetMapping("/available")
    public List<DriverResponseDTO> getAvailableDrivers() {
        return driverService.getAvailableDrivers();
    }

    // 4. PUT /api/drivers/status/{id} (EXISTING)
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
}