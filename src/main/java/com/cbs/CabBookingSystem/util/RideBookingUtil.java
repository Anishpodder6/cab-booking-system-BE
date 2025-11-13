package com.cbs.CabBookingSystem.util;

import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RideBookingUtil {

    private final RideRepository rideRepository;

    public boolean canChangeRideStatus(Ride ride, RideStatus statusToChange) {

        var status = ride.getStatus();
        var driverId = ride.getDriverId();

        if (status == statusToChange) {
            throw new IllegalStateException("Ride is already in the status: " + statusToChange);
        }
        else if (status == RideStatus.Completed) {
            throw new IllegalStateException("Cannot change status of a completed ride.");
        }
        else if (status == RideStatus.CancelledByUser) {
            throw new IllegalStateException("Cannot change status of a cancelled ride.");
        }
        else if(status == RideStatus.CancelledByDriver) {
            if (statusToChange != RideStatus.CancelledByUser && statusToChange != RideStatus.ConfirmedByDriver) {
                throw new IllegalStateException("After CancelledByDriver, status can be ConfirmedByDriver | CancelledByUser.");
            }
            if (statusToChange == RideStatus.ConfirmedByDriver && driverId == null) {
                throw new IllegalStateException("Cannot change status from CancelledByDriver to ConfirmedByDriver without assigning a driver.");
            }

            return true;
        }
        else if (status == RideStatus.ConfirmedByDriver) {
            if (statusToChange != RideStatus.Ongoing && statusToChange != RideStatus.CancelledByUser
                    && statusToChange != RideStatus.CancelledByDriver) {
                throw new IllegalStateException("From ConfirmedByDriver, status can be Ongoing | CancelledByUser | CancelledByDriver.");
            }
            else
                return true;
        }
        else if (status == RideStatus.Ongoing) {
            if (statusToChange != RideStatus.Completed) {
                throw new IllegalStateException("From Ongoing, status can only be changed to Completed.");
            }
            else
                return true;
        }
        else if(status == RideStatus.LookingForDriver) {
            if (statusToChange == RideStatus.CancelledByUser) {
                return true;
            }
            if (driverId == null) {
                throw new IllegalStateException("Cannot change status from LookingForDriver to " + statusToChange + ".");
            }
            if (statusToChange != RideStatus.ConfirmedByDriver) {
                throw new IllegalStateException("From LookingForDriver, status can be ConfirmedByDriver | CancelledByUser.");
            }
            else
                return true;
        }

        return false;
    }

    public UUID extractUUID(String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString((String) uuidStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid UUID ID format: " + uuidStr);
        }

        return uuid;
    }

    public RideStatus extractRideStatus(String statusStr) {
        RideStatus status;
        try {
            status = RideStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Ride Status: " + statusStr);
        }

        return status;
    }


}
