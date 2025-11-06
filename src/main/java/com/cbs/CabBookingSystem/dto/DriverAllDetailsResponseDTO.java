package com.cbs.CabBookingSystem.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverAllDetailsResponseDTO(
        UUID driverId,

        Double todaysEarnings,    // Maps to Today's Earnings
        Integer todaysRides,        // Maps to Today's Rides
        Double driverRating,        // Maps to Driver Rating

        String additionalMetricLabel, // Example: "Acceptance Rate"
        String additionalMetricValue, // Example: "85%"

        // --- Weekly Goals/Summary ---
          Double weeklyEarningsGoal,
          Double weeklyEarningsAchieved,

          Integer weeklyRidesGoal,
          Integer weeklyRidesAchieved,

          Double ratingMaintenanceGoal, // e.g., 4.5
          Double ratingMaintenanceAchieved, // Same as driverRating

          Double acceptanceRateGoal,
          Double acceptanceRateAchieved
) {
}
