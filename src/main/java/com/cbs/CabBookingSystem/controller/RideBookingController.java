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

    @Autowired
    private CarDataRepository carDataRepository;

    //driver,rider
    @GetMapping("/ride/{id}")
    public ResponseEntity<Ride> getRideDetails(@PathVariable UUID id) {
        log.info("Request to fetch ride details for ID: {}", id);
        Ride ride = rideService.getRideById(id);
        log.info("Successfully fetched ride details for ID: {}", id);
        return ResponseEntity.ok(ride);
    }

    //rider
    @PostMapping({"/rider/book"})
    public ResponseEntity<Ride> bookRide(@RequestBody @Valid RideDto rideDto) {
        log.info("Request to book a new ride. Origin: {}, Destination: {}", rideDto.getPickupLocation(), rideDto.getDropLocation());
        log.debug("Ride booking DTO: {}", rideDto);
        Ride bookedRide = rideService.addRide(rideDto);
        log.info("Ride successfully booked with ID: {}", bookedRide.getRideId());
        return ResponseEntity.status(HttpStatus.CREATED).body(bookedRide);
    }

    //rider,driver
    @PatchMapping({"/book/{id}"})
    public ResponseEntity<Ride> patchRideData(@PathVariable UUID id, @RequestBody Map<String, Object> mp) {
        log.info("Request to patch ride data for ID: {}", id);
        log.debug("Patch data for ride ID {}: {}", id, mp);
        Ride newRide = rideService.patchRideData(mp, id);
        log.info("Successfully patched ride data for ID: {}", id);
        return ResponseEntity.ok(newRide);
    }

    //driver
    @GetMapping({"/driver/unassigned-ride"})
    public ResponseEntity<List<Ride>> getUnassignedRides() {
        log.info("Request to fetch all unassigned rides.");
        List<Ride> rides = rideService.getUnassignedRides();
        log.info("Fetched {} unassigned rides.", rides.size());
        return ResponseEntity.ok(rides);
    }

    //rider
    @GetMapping({"/rider/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getRiderUpcomingRides(@PathVariable UUID userId) {
        log.info("Request to fetch upcoming rides for rider ID: {}", userId);
        List<Ride> rides = rideService.getRiderUpcomingRide(userId);
        log.info("Fetched {} upcoming rides for rider ID: {}", rides.size(), userId);
        return ResponseEntity.ok(rides);
    }

    //driver
    @GetMapping({"/driver/upcoming-ride/{userId}"})
    public ResponseEntity<List<Ride>> getDriverUpcomingRides(@PathVariable UUID userId) {
        log.info("Request to fetch upcoming rides for driver ID: {}", userId);
        List<Ride> rides = rideService.getDriverUpcomingRide(userId);
        log.info("Fetched {} upcoming rides for driver ID: {}", rides.size(), userId);
        return ResponseEntity.ok(rides);
    }

    //rider
    @GetMapping({"/rider/{userId}"})
    public ResponseEntity<List<Ride>> getAllRidesForUser(@PathVariable UUID userId) {
        log.info("Request to fetch all rides for user ID: {}", userId);
        List<Ride> rides = rideService.getAllRidesForUser(userId);
        log.info("Fetched {} total rides for user ID: {}", rides.size(), userId);
        return ResponseEntity.ok(rides);
    }

    //rider
    @GetMapping({"/rider/has-two-rides/{userId}"})
    public ResponseEntity<Boolean> hasTwoRides(@PathVariable UUID userId) {
        log.info("Checking if rider ID {} has two active rides.", userId);
        Boolean result = rideService.hasTwoRides(userId);
        log.info("Rider ID {} has two active rides: {}", userId, result);
        return ResponseEntity.ok(result);
    }

    //rider
    @GetMapping({"/rider/get-car-fares"})
    public ResponseEntity<List<CarData>> getAllCallFares() {
        log.info("Request to fetch all car fare data.");
        List<CarData> fares = carDataRepository.findAll();
        log.info("Successfully fetched {} car fare records.", fares.size());
        return ResponseEntity.ok(fares);
    }

    //rider,driver
    @GetMapping({"/status/{id}"})
    public ResponseEntity<RideStatus> getRideStatus(@PathVariable UUID id) {
        log.info("Request to get status for ride ID: {}", id);
        RideStatus status = rideService.getRideStatus(id);
        log.info("Status for ride ID {} is: {}", id, status);
        return ResponseEntity.ok(status);
    }


    @GetMapping("/rider/car-types/history/{userId}")
    public ResponseEntity<Map<String, Long>> getRiderCarTypeCount(@PathVariable UUID userId) {
        log.info("Request to get car type ride count for rider ID: {}", userId);

        Map<String, Long> carTypeCounts = rideService.getCarTypeRideCountForRider(userId);

        if (carTypeCounts == null || carTypeCounts.isEmpty()) {
            log.info("No car type ride history found for rider ID: {}", userId);
            return ResponseEntity.noContent().build();
        }
        log.info("Fetched car type ride counts for rider ID {}. Count: {}", userId, carTypeCounts.size());
        return ResponseEntity.ok(carTypeCounts);
    }
}