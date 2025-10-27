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

@RestController
@RequestMapping("/rides")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200/")
public class RideBookingController {

    private final RideService rideService;

    @Autowired
    private CarDataRepository carDataRepository;

    @GetMapping("/test")
    public String testEndpoint() {
        return "Ride Booking Controller is working!";
    }

    @GetMapping({"/all", "/all/"})
    public ResponseEntity<List<Ride>> getAllRides() {
        return ResponseEntity.ok(rideService.getAllRides());
    }

    @GetMapping("/ride/{id}")
    public ResponseEntity<Ride> getRideDetails(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    @PostMapping({"/book", "/book/"})
    public ResponseEntity<Ride> bookRide(@RequestBody @Valid RideDto rideDto) {
        Ride bookedRide = rideService.addRide(rideDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookedRide);
    }

    @PutMapping({"/book/{id}", "/book/{id}/"})
    public ResponseEntity<Ride> updateRideData(@PathVariable Long id, @RequestBody @Valid RideDto rideDto) {
        Ride newRide = rideService.updateRideData(rideDto, id);
        return ResponseEntity.ok(newRide);
    }

    @PatchMapping({"/book/{id}", "/book/{id}/"})
    public ResponseEntity<Ride> patchRideData(@PathVariable Long id, @RequestBody Map<String, Object> mp) {
        Ride newRide = rideService.patchRideData(mp, id);
        return ResponseEntity.ok(newRide);
    }

    @PatchMapping({"/driver/assign-driver/{rideId}", "/assign-driver/{rideId}/"})
    public ResponseEntity<Boolean> assignDriver(@PathVariable Long rideId, @RequestBody Map<String, Long>mp) {
        return ResponseEntity.ok(rideService.assignDriver(rideId, mp));
    }

    @GetMapping({"/driver/unassigned-ride", "/unassigned-ride/"})
    public ResponseEntity<List<Ride>> getUnassignedRides() {
        return ResponseEntity.ok(rideService.getUnassignedRides());
    }

    @GetMapping({"/rider/upcoming-ride/{userId}", "/rider-upcoming-ride/{userId}/"})
    public ResponseEntity<List<Ride>> getRiderUpcomingRides(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.getRiderUpcomingRide(userId));
    }

    @GetMapping({"/driver/upcoming-ride/{userId}", "/driver/upcoming-ride/{userId}/"})
    public ResponseEntity<List<Ride>> getDriverUpcomingRides(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.getDriverUpcomingRide(userId));
    }

    @GetMapping({"/userId/{userId}", "/userId/{userId}/"})
    public ResponseEntity<List<Ride>> getAllRidesForUser(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.getAllRidesForUser(userId));
    }

    @GetMapping({"/has-two-rides/{userId}", "/has-two-rides/{userId}/"})
    public ResponseEntity<Boolean> hasTwoRides(@PathVariable Long userId) {
        return ResponseEntity.ok(rideService.hasTwoRides(userId));
    }

    @GetMapping({"/get-car-fares", "/get-car-fares/"})
    public ResponseEntity<List<CarData>> getAllCallFares() {
        return ResponseEntity.ok(carDataRepository.findAll());
    }

    @GetMapping({"/status/{id}", "/status/{id}/"})
    public ResponseEntity<RideStatus> getRideStatus(@PathVariable Long id) {
        return ResponseEntity.ok(rideService.getRideStatus(id));
    }
}
