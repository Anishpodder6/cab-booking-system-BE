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
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Specific import for pushing messages
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
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
     * We use a generic '/topic/rides' prefix for simplicity, assuming the
     * WebSocket configuration will route this correctly, matching the Angular client's URL.
     */
    private void pushRideUpdate(UUID rideId, Ride ride) {
        // Matches Angular subscription topic: /topic/rides/{rideId}
        String destination = "/topic/rides/" + rideId;
        messagingTemplate.convertAndSend(destination, ride);
    }

    // --- CRUD Operations ---

    @Override
    public Ride addRide(RideDto rideDto) {

        if (!isUserExists(rideDto.getUserId())) throw new UserNotFound();

        Ride newRide = modelMapper.map(rideDto, Ride.class);

        UUID id = UUID.randomUUID();
        newRide.setRideId(id);
        // Set Looking For Driver Explicitly
        newRide.setStatus(RideStatus.LookingForDriver);

        return rideRepository.save(newRide);
    }

    @Override
    public Ride getRideById(UUID rideId) throws RideNotFound {


        return rideRepository.findById(rideId).orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));
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
        return driverRepository.existsById(driverId);
    }


// --- Retrieval Methods ---

    @Override
    public List<Ride> getRiderUpcomingRide(UUID userId) {
        return rideRepository.findRiderUpcomingRides(userId);
    }

    @Override
    public List<Ride> getUnassignedRides() {
        return rideRepository.findUnassignedRides();
    }

    @Override
    public List<Ride> getDriverUpcomingRide(UUID userId) {
        return rideRepository.findDriverUpcomingRides(userId);
    }


    @Override
    public List<Ride> getAllRidesForUser(UUID userId) {
        return rideRepository.findAllByUserId(userId);
    }


    @Override
    public Boolean hasTwoRides(UUID userId) {
        return rideRepository.countActiveRidesByUserId(userId) >= 2;
    }

    @Override
    public RideStatus getRideStatus(UUID rideId) {
        return rideRepository.findStatusByRideId(rideId);
    }

    @Override
    public List<RideWithRating> getRiderHistory(UUID userId) {
        List<Ride> rideList =  rideRepository.findAllByUserIdOrderByDateTimeDesc(userId);
        List<RideWithRating> rideWithRatingList = new ArrayList<>();

        rideList.forEach(ride -> {
            System.out.println("Got Ride" + ride);
            Rating rating = ratingService.getRatingByRideId(ride.getRideId());
            RideWithRating rideWithRating = modelMapper.map(ride, RideWithRating.class);
            rideWithRating.setRating(rating);
            rideWithRatingList.add(rideWithRating);
        });

        return rideWithRatingList;
    }

    @Override
    public List<RideWithRating> getDriverHistory(UUID driverId) {
        List<Ride> rideList =  rideRepository.findByDriverIdOrderByDateTimeDesc(driverId);
        List<RideWithRating> rideWithRatingList = new ArrayList<>();

        rideList.forEach(ride -> {
            Rating rating = ratingService.getRatingByRideId(ride.getRideId());
            RideWithRating rideWithRating = modelMapper.map(ride, RideWithRating.class);
            rideWithRating.setRating(rating);
            rideWithRatingList.add(rideWithRating);
        });

        return rideWithRatingList;
    }

    @Override
    public Map<String, Long> getCarTypeRideCountForRider(UUID userId) {
        List<Ride> allRides = getAllRidesForUser(userId);
        return allRides.stream()
        .filter(ride -> ride.getCarType() != null && !ride.getCarType().trim().isEmpty())
        .collect(Collectors.groupingBy(
        Ride::getCarType,
        Collectors.counting()
        ));
    }

    private boolean isUserExists(UUID userId) {
        return userRepository.existsById(userId);
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
