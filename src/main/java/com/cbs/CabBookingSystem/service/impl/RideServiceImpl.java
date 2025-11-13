package com.cbs.CabBookingSystem.service.impl;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.DriverNotFound;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import com.cbs.CabBookingSystem.exception.customexception.UserNotFound;
import com.cbs.CabBookingSystem.model.Rating;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.RideWithRating;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import com.cbs.CabBookingSystem.service.RatingService;
import com.cbs.CabBookingSystem.service.RideService;
import com.cbs.CabBookingSystem.util.RideBookingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Specific import for pushing messages
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final RatingService ratingService;
    private final SimpMessagingTemplate messagingTemplate; // <-- Injected for WebSocket communication
    private final ModelMapper modelMapper;
    private final RideBookingUtil rideBookingUtil;

    /**
     * Helper method to push the updated Ride object to the client.
     * The destination must match the Angular client's subscription path.
     */
    private void pushRideUpdate(UUID rideId, Ride ride) {
        String destination = "/topic/rides/" + rideId;
        log.info("Pushing ride update to WebSocket destination: {}", destination);
        messagingTemplate.convertAndSend(destination, ride);
    }

    // --- CRUD Operations ---

    @Override
    public Ride addRide(RideDto rideDto) {
        log.info("Attempting to add new ride for user: {}", rideDto.getUserId());

        if (!isUserExists(rideDto.getUserId())) {
            log.error("Failed to add ride. User not found: {}", rideDto.getUserId());
            throw new UserNotFound(rideDto.getUserId());
        }

        Ride newRide = modelMapper.map(rideDto, Ride.class);

        UUID id = UUID.randomUUID();
        newRide.setRideId(id);
        newRide.setStatus(RideStatus.LookingForDriver);

        Ride savedRide = rideRepository.save(newRide);
        log.info("New ride created successfully with ID: {}", id);
        log.debug("New Ride Details: {}", savedRide);
        return savedRide;

    }

    @Override
    public Ride getRideById(UUID rideId) throws RideNotFound {
        log.info("Attempting to retrieve ride by ID: {}", rideId);
        return rideRepository.findById(rideId)
                .orElseThrow(() -> {
                    log.error("Ride not found with ID: {}", rideId);
                    return new RideNotFound("Ride with id " + rideId + " not found");
                });
    }


    @Override
    public Ride patchRideData(Map<String, Object> mp, UUID rideId) throws RideNotFound {
        // Allow only one field update at a time
        if (mp.size() != 1) {
            throw new IllegalArgumentException("Only one field can be updated at a time");
        }

        // Fetch existing ride or throw if not found
        Ride existingRide = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));

        String key = mp.keySet().iterator().next();
        Object value = mp.get(key);

        switch (key) {
            case "driverId" -> {
                // Validate driverId and check if driver exists
                if (value == null || "null".equalsIgnoreCase(String.valueOf(value))) {
                    throw new IllegalArgumentException("driverId cannot be null");
                }
                UUID driverID = rideBookingUtil.extractUUID((String) value);

                if (!isDriverExists(driverID)) throw new DriverNotFound(driverID);

                if (canAssignDriver(rideId, driverID))  {
                    // Assign driver and update status
                    existingRide.setDriverId(driverID);
                    existingRide.setStatus(RideStatus.ConfirmedByDriver);
                }
            }
            case "status" -> {
                // Validate and update ride status
                RideStatus newStatus = rideBookingUtil.extractRideStatus((String) value);

                if (rideBookingUtil.canChangeRideStatus(existingRide, newStatus)) {
                    if (existingRide.getDriverId() != null && newStatus == RideStatus.Ongoing) {
                        boolean hasPreviousOngoingRide = rideRepository.hasPreviousOngoingRide(existingRide.getDriverId());
                        if (hasPreviousOngoingRide) {
                            throw new IllegalStateException("First complete the ongoing ride");
                        }
                    }
                    existingRide.setStatus(newStatus);

                    // Remove driver if cancelled by driver
                    if (newStatus == RideStatus.CancelledByDriver) {
                        existingRide.setDriverId(null);
                    }
                }
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + key);
        }

        // Save and push update
        Ride updatedRide = rideRepository.save(existingRide);
        pushRideUpdate(rideId, updatedRide);
        return updatedRide;
    }

    private boolean isDriverExists(UUID driverId) {
        boolean exists = driverRepository.existsById(driverId);
        log.debug("Driver existence check for ID {}: {}", driverId, exists);
        return exists;
    }


// --- Retrieval Methods ---

    @Override
    public List<Ride> getRiderUpcomingRide(UUID userId) {
        log.info("Fetching upcoming rides for rider ID: {}", userId);
        List<Ride> rides = rideRepository.findRiderUpcomingRides(userId);
        log.debug("Found {} upcoming rides for rider ID: {}", rides.size(), userId);
        return rides;
    }

    @Override
    public List<Ride> getUnassignedRides() {
        log.info("Fetching all unassigned rides.");
        List<Ride> rides = rideRepository.findUnassignedRides();
        log.debug("Found {} unassigned rides.", rides.size());
        return rides;
    }

    @Override
    public List<Ride> getDriverUpcomingRide(UUID userId) {
        log.info("Fetching upcoming rides for driver ID: {}", userId);
        List<Ride> rides = rideRepository.findDriverUpcomingRides(userId);
        log.debug("Found {} upcoming rides for driver ID: {}", rides.size(), userId);
        return rides;
    }


    @Override
    public List<Ride> getAllRidesForUser(UUID userId) {
        log.info("Fetching all rides for user ID: {}", userId);
        List<Ride> rides = rideRepository.findAllByUserId(userId);
        log.debug("Found {} total rides for user ID: {}", rides.size(), userId);
        return rides;
    }


    @Override
    public Boolean hasTwoRides(UUID userId) {
        long activeCount = rideRepository.countActiveRidesByUserId(userId);
        boolean hasTwo = activeCount >= 2;
        log.debug("User {} has {} active rides. Result: {}", userId, activeCount, hasTwo);
        return hasTwo;
    }

    @Override
    public RideStatus getRideStatus(UUID rideId) {
        RideStatus status = rideRepository.findStatusByRideId(rideId);
        log.debug("Current status for ride {}: {}", rideId, status);
        return status;
    }

    @Override
    public List<RideWithRating> getRiderHistory(UUID userId) {
        log.info("Fetching ride history with ratings for rider ID: {}", userId);
        List<Ride> rideList =  rideRepository.findAllByUserIdOrderByDateTimeDesc(userId);
        List<RideWithRating> rideWithRatingList = new ArrayList<>();

        rideList.forEach(ride -> {
            log.debug("Processing ride {} for history.", ride.getRideId());
            Rating rating = ratingService.getRatingByRideId(ride.getRideId());
            RideWithRating rideWithRating = modelMapper.map(ride, RideWithRating.class);
            rideWithRating.setRating(rating);
            rideWithRatingList.add(rideWithRating);
        });
        log.info("Finished processing {} rides for rider history.", rideList.size());
        return rideWithRatingList;
    }

    @Override
    public List<RideWithRating> getDriverHistory(UUID driverId) {
        log.info("Fetching ride history with ratings for driver ID: {}", driverId);
        List<Ride> rideList =  rideRepository.findByDriverIdOrderByDateTimeDesc(driverId);
        List<RideWithRating> rideWithRatingList = new ArrayList<>();

        rideList.forEach(ride -> {
            log.debug("Processing ride {} for driver history.", ride.getRideId());
            Rating rating = ratingService.getRatingByRideId(ride.getRideId());
            RideWithRating rideWithRating = modelMapper.map(ride, RideWithRating.class);
            rideWithRating.setRating(rating);
            rideWithRatingList.add(rideWithRating);
        });
        log.info("Finished processing {} rides for driver history.", rideList.size());
        return rideWithRatingList;
    }

    @Override
    public Map<String, Long> getCarTypeRideCountForRider(UUID userId) {
        log.info("Calculating car type ride count for rider ID: {}", userId);
        List<Ride> allRides = getAllRidesForUser(userId);
        Map<String, Long> carTypeCounts = allRides.stream()
                .filter(ride -> ride.getCarType() != null && !ride.getCarType().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Ride::getCarType,
                        Collectors.counting()
                ));
        log.debug("Car type counts: {}", carTypeCounts);
        return carTypeCounts;
    }

    private boolean isUserExists(UUID userId) {
        boolean exists = userRepository.existsById(userId);
        log.debug("User existence check for ID {}: {}", userId, exists);
        return exists;
    }

    private boolean canAssignDriver(UUID rideId, UUID driverId) {
        boolean isRideAssignedAlready = rideRepository.existsByRideIdAndDriverIdIsNotNull(rideId);
        if (isRideAssignedAlready) {
            throw new AlreadyRideAssignedException("Ride is Already Assigned");
        }
        boolean isHavingTwoActiveRides = rideRepository.countActiveRidesByDriverId(driverId) >= 2;

        if (isHavingTwoActiveRides) {
            throw new IllegalStateException("Driver cannot have more than two active rides");
        }
        return true;
    }

}