package com.cbs.CabBookingSystem.controller;
import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.model.CarData;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.CarDataRepository;
import com.cbs.CabBookingSystem.service.RideService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class RideBookingController {

    private final RideService rideService;
    private final CarDataRepository carDataRepository;

    //driver,rider
    @GetMapping("/ride/{id}")
    public ResponseEntity<Ride> getRideDetails(@PathVariable UUID id) {
        log.info("Successfully fetched ride details for ID: {}", id);
        return ResponseEntity.ok(rideService.getRideById(id));
    }

    //rider
    @PostMapping({"/rider/book"})
    public ResponseEntity<Ride> bookRide(@RequestBody @Valid RideDto rideDto) {
        Ride bookedRide = rideService.addRide(rideDto);
        log.info("Ride successfully booked with ID: {}", bookedRide.getRideId());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookedRide);
    }

    //rider,driver
    @PatchMapping({"/book/{id}"})
    public ResponseEntity<Ride> patchRideData(@PathVariable UUID id, @RequestBody Map<String, Object> mp) {
        Ride newRide = rideService.patchRideData(mp, id);
        log.info("Successfully patched ride data for ID: {}", id);
        return ResponseEntity.ok(newRide);
    }

    //driver
    @GetMapping({"/driver/unassigned-ride"})
    public ResponseEntity<List<Ride>> getUnassignedRides() {
        log.info("Fetched {} unassigned rides.", rideService.getUnassignedRides());
        return ResponseEntity.ok(rideService.getUnassignedRides());
    }

    //rider
    @GetMapping({"/rider/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getRiderUpcomingRides(@PathVariable UUID userId) {
        log.info("Fetched {} upcoming rides for rider ID: {}", rideService.getRiderUpcomingRide(userId), userId);
        return ResponseEntity.ok(rideService.getRiderUpcomingRide(userId));
    }

    //driver
    @GetMapping({"/driver/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getDriverUpcomingRides(@PathVariable UUID userId) {
        log.info("Fetched {} upcoming rides for driver ID: {}", rideService.getDriverUpcomingRide(userId), userId);
        return ResponseEntity.ok(rideService.getDriverUpcomingRide(userId));
    }


    @GetMapping({"/rider/has-two-rides/{userId}"})
    public ResponseEntity<Boolean> hasTwoRides(@PathVariable UUID userId) {
        log.info("Rider ID {} has two active rides: {}", userId, rideService.hasTwoRides(userId));
        return ResponseEntity.ok(rideService.hasTwoRides(userId));
    }

    //rider
    @GetMapping({"/rider/get-car-fares"})
    public ResponseEntity<List<CarData>> getAllCallFares() {
        log.info("Successfully fetched {} car fare records.", carDataRepository.findAll());
        return ResponseEntity.ok(carDataRepository.findAll());
    }

    //rider,driver
    @GetMapping({"/status/{id}"})
    public ResponseEntity<RideStatus> getRideStatus(@PathVariable UUID id) {
        log.info("Status for ride ID {} is: {}", id, rideService.getRideStatus(id));
        return ResponseEntity.ok(rideService.getRideStatus(id));
    }


    @GetMapping("/rider/car-types/history/{userId}")
    public ResponseEntity<Map<String, Long>> getRiderCarTypeCount(@PathVariable UUID userId) {

        Map<String, Long> carTypeCounts = rideService.getCarTypeRideCountForRider(userId);

        if (carTypeCounts == null || carTypeCounts.isEmpty()) {
            log.info("No car type ride history found for rider ID: {}", userId);
            return ResponseEntity.noContent().build();
        }
        log.info("Fetched car type ride counts for rider ID {}. Count: {}", userId, carTypeCounts.size());
        return ResponseEntity.ok(carTypeCounts);
    }
}
