package com.cbs.vector.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record DriverAllDetailsResponseDTO(
        UUID driverId,

        Double todaysEarnings,
        Integer todaysRides,
        Double driverRating,

        String additionalMetricLabel,
        String additionalMetricValue,

          Double weeklyEarningsGoal,
          Double weeklyEarningsAchieved,

          Integer weeklyRidesGoal,
          Integer weeklyRidesAchieved,

          Double ratingMaintenanceGoal,
          Double ratingMaintenanceAchieved,

          Double acceptanceRateGoal,
          Double acceptanceRateAchieved
) {
}
