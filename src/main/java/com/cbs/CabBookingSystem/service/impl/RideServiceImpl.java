package com.cbs.CabBookingSystem.service.impl;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.PaymentMethod;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.service.RideService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    @Autowired
    private ModelMapper modelMapper;

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
        return rideRepository.save(existingRide);
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
                case "userId" -> existingRide.setUserId(Long.valueOf(String.valueOf(value)));
                case "pickupLocation" -> existingRide.setPickupLocation((String) value);
                case "dropLocation" -> existingRide.setDropLocation((String) value);
                case "driverId" -> {
                    if (value == null || "null".equalsIgnoreCase(String.valueOf(value))) {
                        existingRide.setDriverId(null); // explicitly set to null
                    } else {
                        boolean isRideAssignedAlready = rideRepository.existsByRideIdAndDriverIdIsNotNull(rideId);

                        if (isRideAssignedAlready) {
                            throw new AlreadyRideAssignedException("Ride is Already Assigned");
                        }
                        existingRide.setDriverId(Long.valueOf(String.valueOf(value)));
                        existingRide.setStatus(RideStatus.ConfirmedByDriver);
                    }
                }
                case "carType" -> existingRide.setCarType((String) value);
                case "fare" -> existingRide.setFare(Double.valueOf(String.valueOf(value)));
                case "status" -> existingRide.setStatus(RideStatus.valueOf((String) value));
//                case "paymentMethod" -> existingRide.setPaymentMethod(PaymentMethod.valueOf((String) value));
                default -> throw new IllegalStateException("Unexpected value: " + key);
            }
        });

        return rideRepository.save(existingRide);

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
//        existingRide.setPaymentMethod(rideDto.getPaymentMethod());

        return rideRepository.save(existingRide);
    }

    @Override
    public Boolean assignDriver(Long rideId, Map<String, Long>mp) throws RideNotFound, IllegalArgumentException, AlreadyRideAssignedException {
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

        return true;
    }

    @Override
    public List<Ride> getRiderUpcomingRide(Long userId) {
        return rideRepository.findRiderUpcomingRides(userId);
    }

    @Override
    public List<Ride> getUnassignedRides() {
        return rideRepository.findUnassignedRides();
    }

    @Override
    public List<Ride> getDriverUpcomingRide(Long userId) {
        return rideRepository.findDriverUpcomingRides(userId);
    }

    @Override
    public List<Ride> getAllRidesForUser(Long userId) {
        return rideRepository.findAllByUserId(userId);
    }

    @Override
    public List<Ride> getAllRides() {
        return rideRepository.findAll();
    }

    @Override
    public Boolean hasTwoRides(Long userId) {
        return rideRepository.countActiveRidesByUserId(userId) >= 2;
    }

    @Override
    public RideStatus getRideStatus(Long rideId) {
        return rideRepository.findStatusByRideId(rideId);
    }
}
