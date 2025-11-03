package com.cbs.CabBookingSystem.service.impl;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.service.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Specific import for pushing messages
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final SimpMessagingTemplate messagingTemplate; // <-- Injected for WebSocket communication

    @Autowired
    private ModelMapper modelMapper;

    /**
     * Helper method to push the updated Ride object to the client.
     * The destination must match the Angular client's subscription path.
     * We use a generic '/topic/rides' prefix for simplicity, assuming the
     * WebSocket configuration will route this correctly, matching the Angular client's URL.
     */
    private void pushRideUpdate(Long rideId, Ride ride) {
        // Matches Angular subscription topic: /topic/rides/{rideId}
        String destination = "/topic/rides/" + rideId;
        messagingTemplate.convertAndSend(destination, ride);
    }

    // --- CRUD Operations ---

    @Override
    public Ride addRide(RideDto rideDto) {
        Ride newRide = modelMapper.map(rideDto, Ride.class);
        return rideRepository.save(newRide);
    }

    @Override
    public Ride getRideById(Long rideId) throws RideNotFound{


        return rideRepository.findById(rideId).orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));
    }

    @Override
    public Ride updateRideStatus(Long rideId, String status) throws RideNotFound{
        Ride existingRide = rideRepository.findById(rideId).orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));
        existingRide.setStatus(RideStatus.valueOf(status));
        Ride updatedRide = rideRepository.save(existingRide);

        // REACTIVE UPDATE: Push the change
        pushRideUpdate(rideId, updatedRide);

        return updatedRide;
    }

    @Override
    public Boolean deleteRide(Long rideId) {
        boolean isDeleted = false;
        if (rideRepository.existsById(rideId)) {
            rideRepository.deleteById(rideId);
            isDeleted = true;
        }
        return isDeleted;
    }

    @Override
    public Ride patchRideData(Map<String, Object>mp, Long rideId) throws RideNotFound {
        Ride existingRide = rideRepository.findById(rideId).orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));

        mp.forEach((key, value) -> {
            switch (key) {
                case "userId" -> existingRide.setUserId((UUID) value);
                case "pickupLocation" -> existingRide.setPickupLocation((String) value);
                case "dropLocation" -> existingRide.setDropLocation((String) value);
                case "driverId" -> {
                    if (value == null || "null".equalsIgnoreCase(String.valueOf(value))) {
                        existingRide.setDriverId(null); // explicitly set to null
                    } else {
                        // Check if driverId is actually changing to prevent unnecessary exception
                        boolean isRideAssignedAlready = rideRepository.existsByRideIdAndDriverIdIsNotNull(rideId);

                        if (isRideAssignedAlready) {
                            throw new AlreadyRideAssignedException("Ride is Already Assigned");
                        }
                        existingRide.setDriverId((String) value);
                        existingRide.setStatus(RideStatus.ConfirmedByDriver);
                    }
                    existingRide.setDriverId((UUID)value);
                    existingRide.setStatus(RideStatus.ConfirmedByDriver);
                }
            case "carType" -> existingRide.setCarType((String) value);
            case "fare" -> existingRide.setFare(Double.valueOf(String.valueOf(value)));
            case "status" -> existingRide.setStatus(RideStatus.valueOf((String) value));
            // case "paymentMethod" -> existingRide.setPaymentMethod(PaymentMethod.valueOf((String) value));
            default -> throw new IllegalStateException("Unexpected value: " + key);
        }
    });

    Ride updatedRide = rideRepository.save(existingRide);

    // REACTIVE UPDATE: Push the change
    pushRideUpdate(rideId, updatedRide);

        return updatedRide;
}

@Override
public Ride updateRideData(RideDto rideDto, Long rideId) throws RideNotFound{
    Ride existingRide = rideRepository.findById(rideId).orElseThrow(() -> new RideNotFound("Ride with id " + rideId + " not found"));

    existingRide.setUserId(rideDto.getUserId());
    existingRide.setPickupLocation(rideDto.getPickupLocation());
    existingRide.setDropLocation(rideDto.getDropLocation());
    existingRide.setDriverId(rideDto.getDriverId());
    existingRide.setCarType(rideDto.getCarType());
    existingRide.setFare(rideDto.getFare());
    existingRide.setStatus(rideDto.getStatus());
    // existingRide.setPaymentMethod(rideDto.getPaymentMethod());

    Ride updatedRide = rideRepository.save(existingRide);

    // REACTIVE UPDATE: Push the change
    pushRideUpdate(rideId, updatedRide);

    return updatedRide;
}

@Override
public Boolean assignDriver(Long rideId, Map<String, String>mp) throws RideNotFound, IllegalArgumentException, AlreadyRideAssignedException {
    if (!rideRepository.existsById(rideId)) {
        throw new RideNotFound("Ride Not Found");
    }
    if (!mp.containsKey("driverId")) {
        throw new IllegalArgumentException("No driverId present");
    }

    var driverId = mp.get("driverId");

    boolean isRideAssignedAlready = rideRepository.existsByRideIdAndDriverIdIsNotNull(rideId);

    if (isRideAssignedAlready) {
        throw new AlreadyRideAssignedException("Ride is Already Assigned");
    }

    rideRepository.updateDriverIdByRideId(rideId, driverId);

    // After assigning a driver via raw repository method, we need to fetch the updated entity
    // to push the complete, updated Ride object.
    Ride updatedRide = getRideById(rideId);

    // REACTIVE UPDATE: Push the change
    pushRideUpdate(rideId, updatedRide);

    return true;
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
<<<<<<< HEAD
public List<Ride> getDriverUpcomingRide(UUID userId) {
=======
    public List<Ride> getDriverUpcomingRide(String userId) {
>>>>>>> 0a9288c (WIP: Security Pending, Added Fetching of Driver Details, Security filters config for ride)
        return rideRepository.findDriverUpcomingRides(userId);
    }

    @Override
    public List<Ride> getAllRidesForUser(UUID userId) {
        return rideRepository.findAllByUserId(userId);
    }

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public Boolean hasTwoRides(UUID userId) {
        return rideRepository.countActiveRidesByUserId(userId) >= 2;
    }

    @Override
    public RideStatus getRideStatus(Long rideId) {
        return rideRepository.findStatusByRideId(rideId);
    }
}