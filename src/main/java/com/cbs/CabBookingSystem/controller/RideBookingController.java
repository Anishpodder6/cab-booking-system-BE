package com.cbs.CabBookingSystem.controller;
import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.model.CarData;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.CarDataRepository;
import com.cbs.CabBookingSystem.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200/")
public class RideBookingController {

    private final RideService rideService;

    @Autowired
    private CarDataRepository carDataRepository;

    //driver,rider
    @GetMapping("/rider/ride/{id}")
    public ResponseEntity<Ride> getRideDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    //rider
    @PostMapping({"/rider/book"})
    public ResponseEntity<Ride> bookRide(@RequestBody @Valid RideDto rideDto) {
        Ride bookedRide = rideService.addRide(rideDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookedRide);
    }

    //rider,driver
    @PatchMapping({"/rider/book/{id}"})
    public ResponseEntity<Ride> patchRideData(@PathVariable UUID id, @RequestBody Map<String, Object> mp) {
        Ride newRide = rideService.patchRideData(mp, id);
        return ResponseEntity.ok(newRide);
    }

    //driver
    @GetMapping({"/driver/unassigned-ride"})
    public ResponseEntity<List<Ride>> getUnassignedRides() {
        return ResponseEntity.ok(rideService.getUnassignedRides());
    }

    //rider
    @GetMapping({"/rider/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getRiderUpcomingRides(@PathVariable UUID userId) {
        return ResponseEntity.ok(rideService.getRiderUpcomingRide(userId));
    }

    //driver
    @GetMapping({"/driver/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getDriverUpcomingRides(@PathVariable UUID userId) {
        return ResponseEntity.ok(rideService.getDriverUpcomingRide(userId));
    }

    //rider
    @GetMapping({"/rider/{userId}"})
    public ResponseEntity<List<Ride>> getAllRidesForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(rideService.getAllRidesForUser(userId));
    }

    //rider
    @GetMapping({"/rider/has-two-rides/{userId}"})
    public ResponseEntity<Boolean> hasTwoRides(@PathVariable UUID userId) {
        return ResponseEntity.ok(rideService.hasTwoRides(userId));
    }

    //rider
    @GetMapping({"/rider/get-car-fares"})
    public ResponseEntity<List<CarData>> getAllCallFares() {
        return ResponseEntity.ok(carDataRepository.findAll());
    }

    //rider,driver
    @GetMapping({"/status/{id}"})
    public ResponseEntity<RideStatus> getRideStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(rideService.getRideStatus(id));
    }
}
