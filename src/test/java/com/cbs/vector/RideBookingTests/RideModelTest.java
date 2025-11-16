package com.cbs.vector.RideBookingTests;

import com.cbs.vector.model.Ride;
import com.cbs.vector.model.enums.RideStatus;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RideModelTest {

    @Test
    void testRideFieldsAndDefaults() {
        UUID rideId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID driverId = UUID.randomUUID();
        String pickupLocation = "A";
        String dropLocation = "B";
        String carType = "Sedan";
        Double fare = 50.0;
        String receipientEmail = "test@example.com";

        Ride ride = new Ride();
        ride.setRideId(rideId);
        ride.setUserId(userId);
        ride.setPickupLocation(pickupLocation);
        ride.setDropLocation(dropLocation);
        ride.setDriverId(driverId);
        ride.setCarType(carType);
        ride.setFare(fare);
        ride.setReceipientEmail(receipientEmail);

        assertEquals(rideId, ride.getRideId());
        assertEquals(userId, ride.getUserId());
        assertEquals(pickupLocation, ride.getPickupLocation());
        assertEquals(dropLocation, ride.getDropLocation());
        assertEquals(driverId, ride.getDriverId());
        assertEquals(carType, ride.getCarType());
        assertEquals(fare, ride.getFare());
        assertEquals(receipientEmail, ride.getReceipientEmail());

        assertEquals(RideStatus.LookingForDriver, ride.getStatus());

        assertNull(ride.getDateTime());
    }
}
