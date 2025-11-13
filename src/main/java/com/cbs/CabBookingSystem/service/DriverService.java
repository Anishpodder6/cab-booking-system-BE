package com.cbs.CabBookingSystem.service;
import com.cbs.CabBookingSystem.repository.PaymentRepository;
import com.cbs.CabBookingSystem.repository.RatingRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional; // Added for robustness
import com.cbs.CabBookingSystem.dto.*;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.*;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
@Slf4j
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RideRepository rideRepository;

    // ==========================================================
    //                        HELPER METHODS
    // ==========================================================

    /**
     * Converts a DriverRegistrationDTO (API Request) to a Driver Entity (JPA/Database).
     * @param dto The registration data received from the controller.
     * @return The Driver entity ready for persistence.
     */
    private Driver convertToEntity(DriverRegistrationDTO dto) {
        log.debug("Converting DriverRegistrationDTO to Driver entity for email: {}", dto.getPersonalDetails().getEmail());
        Driver driver = new Driver();

        // Map Personal Details
        PersonalDetails pd = new PersonalDetails();
        pd.setFirstName(dto.getPersonalDetails().getFirstName());
        pd.setLastName(dto.getPersonalDetails().getLastName());
        pd.setEmail(dto.getPersonalDetails().getEmail());
        pd.setPhone(dto.getPersonalDetails().getPhone());
        pd.setDateOfBirth(dto.getPersonalDetails().getDateOfBirth());
        pd.setPassword(dto.getPersonalDetails().getPassword());
        pd.setConfirmPassword(dto.getPersonalDetails().getConfirmPassword());
        driver.setPersonalDetails(pd);
        log.debug("Personal Details mapped.");

        // Map Driver Details
        DriverDetails dd = new DriverDetails();
        dd.setLicenseNumber(dto.getDriverDetails().getLicenseNumber());
        dd.setLicenseExpiry(dto.getDriverDetails().getLicenseExpiry());
        dd.setExperience(dto.getDriverDetails().getExperience());
        dd.setEmergencyName(dto.getDriverDetails().getEmergencyName());
        dd.setEmergencyPhone(dto.getDriverDetails().getEmergencyPhone());
        dd.setEmergencyRelation(dto.getDriverDetails().getEmergencyRelation());
        driver.setDriverDetails(dd);
        log.debug("Driver Details mapped.");

        // Map Vehicle Details
        VehicleDetails vd = new VehicleDetails();
        vd.setVehicleNumber(dto.getVehicleDetails().getVehicleNumber());
        vd.setVehicleMake(dto.getVehicleDetails().getVehicleMake());
        vd.setVehicleModel(dto.getVehicleDetails().getVehicleModel());
        vd.setVehicleYear(dto.getVehicleDetails().getVehicleYear());
        vd.setVehicleColor(dto.getVehicleDetails().getVehicleColor());
        driver.setVehicleDetails(vd);
        log.debug("Vehicle Details mapped.");

        // Map Banking Details
        BankingDetails bd = new BankingDetails();
        bd.setBankAccount(dto.getBankingDetails().getBankAccount());
        bd.setRoutingNumber(dto.getBankingDetails().getRoutingNumber());
        driver.setBankingDetails(bd);
        log.debug("Banking Details mapped.");

        return driver;
    }

    /**
     * Converts a Driver Entity (JPA/Database) to a DriverResponseDTO (API Response).
     * @param driver The entity retrieved from the database.
     * @return The response DTO sent back to the controller (excluding sensitive data like raw password).
     */
    public DriverResponseDTO convertToDto(Driver driver) {
        log.debug("Converting Driver entity to DriverResponseDTO for ID: {}", driver.getId());
        DriverResponseDTO dto = new DriverResponseDTO();
        dto.setId(driver.getId());
        dto.setRole(driver.getDriverRole());
        dto.setStatus(driver.getStatus());
        dto.setName(driver.getName());
        dto.setCreatedAt(driver.getCreatedAt());
        dto.setUpdatedAt(driver.getUpdatedAt());

        // Map Personal Details (Response Safe DTO)
        DriverResponseDTO.PersonalDetailsResponseDTO pdDTO = new DriverResponseDTO.PersonalDetailsResponseDTO();
        pdDTO.setFirstName(driver.getPersonalDetails().getFirstName());
        pdDTO.setLastName(driver.getPersonalDetails().getLastName());
        pdDTO.setEmail(driver.getPersonalDetails().getEmail());
        pdDTO.setPhone(driver.getPersonalDetails().getPhone());
        pdDTO.setDateOfBirth(driver.getPersonalDetails().getDateOfBirth());
        dto.setPersonalDetails(pdDTO);

        // Map Driver Details
        DriverDetailsDTO ddDTO = new DriverDetailsDTO();
        ddDTO.setLicenseNumber(driver.getDriverDetails().getLicenseNumber());
        ddDTO.setLicenseExpiry(driver.getDriverDetails().getLicenseExpiry());
        ddDTO.setExperience(driver.getDriverDetails().getExperience());
        ddDTO.setEmergencyName(driver.getDriverDetails().getEmergencyName());
        ddDTO.setEmergencyPhone(driver.getDriverDetails().getEmergencyPhone());
        ddDTO.setEmergencyRelation(driver.getDriverDetails().getEmergencyRelation());
        dto.setDriverDetails(ddDTO);

        // Map Vehicle Details
        VehicleDetailsDTO vdDTO = new VehicleDetailsDTO();
        vdDTO.setVehicleNumber(driver.getVehicleDetails().getVehicleNumber());
        vdDTO.setVehicleMake(driver.getVehicleDetails().getVehicleMake());
        vdDTO.setVehicleModel(driver.getVehicleDetails().getVehicleModel());
        vdDTO.setVehicleYear(driver.getVehicleDetails().getVehicleYear());
        vdDTO.setVehicleColor(driver.getVehicleDetails().getVehicleColor());
        dto.setVehicleDetails(vdDTO);

        // Map Banking Details
        BankingDetailsDTO bdDTO = new BankingDetailsDTO();
        bdDTO.setBankAccount(driver.getBankingDetails().getBankAccount());
        bdDTO.setRoutingNumber(driver.getBankingDetails().getRoutingNumber());
        dto.setBankingDetails(bdDTO);

        log.debug("Driver DTO conversion complete for ID: {}", driver.getId());
        return dto;
    }

    // ==========================================================
    //                        API METHODS
    // ==========================================================

    /**
     * 1. POST /api/drivers/register
     * Converts DTO to Entity, saves it, and converts the result back to DTO.
     */
    public DriverResponseDTO registerDriver(DriverRegistrationDTO registrationDTO) {
        log.info("Attempting to register new driver with email: {}", registrationDTO.getPersonalDetails().getEmail());
        Driver driver = convertToEntity(registrationDTO);
        Driver savedDriver = driverRepository.save(driver);
        log.info("Driver successfully registered with ID: {}", savedDriver.getId());
        return convertToDto(savedDriver);
    }

    /**
     * 3. GET /api/drivers/available
     * Finds all drivers with the AVAILABLE status and returns them as a list of DTOs.
     */
    public List<DriverResponseDTO> getAvailableDrivers() {
        log.info("Fetching all available drivers.");
        List<DriverResponseDTO> availableDrivers = driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .map(this::convertToDto) // Convert each entity to a DTO
                .collect(Collectors.toList());
        log.info("Found {} available drivers.", availableDrivers.size());
        return availableDrivers;
    }

    /**
     * 4. PUT /api/drivers/status/{id}
     * Finds driver by ID, updates status, saves it, and returns the updated DTO.
     */
    @Transactional
    public Optional<DriverResponseDTO> updateDriverStatus(UUID id, DriverStatus newStatus) {
        log.info("Attempting to update status for driver ID {} to {}", id, newStatus);
        return driverRepository.findById(id).map(driver -> {
            DriverStatus oldStatus = driver.getStatus();
            driver.setStatus(newStatus);
            driver.setUpdatedAt(LocalDateTime.now()); // Manually set updatedAt as a good practice
            Driver updatedDriver = driverRepository.save(driver);
            log.info("Driver ID {} status updated from {} to {}", id, oldStatus, newStatus);
            return convertToDto(updatedDriver); // Convert updated Entity to DTO
        });
    }

    // FOR PUT in Drivers Profile
    @Transactional
    public DriverResponseDTO updateDriverProfile(UUID id, DriverUpdateDTO updateDTO) {
        log.info("Attempting to update profile for driver ID: {}", id);
        Optional<Driver> driverOpt = driverRepository.findById(id);

        if (driverOpt.isEmpty()) {
            log.warn("Driver with ID {} not found for update operation.", id);
            throw new ResourceNotFoundException("Driver not found with ID : " + id);
        }

        Driver driver = driverOpt.get();

        // --- 1. Update Personal Details ---
        PersonalDetails pd = driver.getPersonalDetails();
        log.debug("Personal Details before update: {}", pd);

        if (updateDTO.getFirstName() != null) pd.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getLastName() != null) pd.setLastName(updateDTO.getLastName());
        if (updateDTO.getPhone() != null) pd.setPhone(updateDTO.getPhone());
        if (updateDTO.getDateOfBirth() != null) pd.setDateOfBirth(updateDTO.getDateOfBirth());
        driver.setPersonalDetails(pd);
        log.debug("Personal Details updated: {}", pd);

        // --- 2. Update Driver Details ---
        DriverDetails dd = driver.getDriverDetails();
        if (updateDTO.getLicenseNumber() != null) dd.setLicenseNumber(updateDTO.getLicenseNumber());
        if (updateDTO.getLicenseExpiry() != null) dd.setLicenseExpiry(updateDTO.getLicenseExpiry());
        if (updateDTO.getExperience() != null) dd.setExperience(updateDTO.getExperience());
        if (updateDTO.getEmergencyName() != null) dd.setEmergencyName(updateDTO.getEmergencyName());
        if (updateDTO.getEmergencyPhone() != null) dd.setEmergencyPhone(updateDTO.getEmergencyPhone());
        if (updateDTO.getEmergencyRelation() != null) dd.setEmergencyRelation(updateDTO.getEmergencyRelation());
        driver.setDriverDetails(dd);
        log.debug("Driver Details updated.");


        // --- 3. Update Vehicle Details ---
        VehicleDetails vd = driver.getVehicleDetails();
        if (updateDTO.getVehicleNumber() != null) vd.setVehicleNumber(updateDTO.getVehicleNumber());
        if (updateDTO.getVehicleMake() != null) vd.setVehicleMake(updateDTO.getVehicleMake());
        if (updateDTO.getVehicleModel() != null) vd.setVehicleModel(updateDTO.getVehicleModel());
        if (updateDTO.getVehicleYear() != null) vd.setVehicleYear(updateDTO.getVehicleYear());
        if (updateDTO.getVehicleColor() != null) vd.setVehicleColor(updateDTO.getVehicleColor());
        driver.setVehicleDetails(vd);
        log.debug("Vehicle Details updated.");


        // --- 4. Update Banking Details ---
        BankingDetails bd = driver.getBankingDetails();
        if (updateDTO.getBankAccount() != null) bd.setBankAccount(updateDTO.getBankAccount());
        if (updateDTO.getRoutingNumber() != null) bd.setRoutingNumber(updateDTO.getRoutingNumber());
        driver.setBankingDetails(bd);
        log.debug("Banking Details updated.");


        // --- 5. Update Status (Optional) ---
        if (updateDTO.getStatus() != null) {
            DriverStatus oldStatus = driver.getStatus();
            driver.setStatus(updateDTO.getStatus());
            log.info("Driver ID {} status changed from {} to {}", id, oldStatus, updateDTO.getStatus());
        }

        driver.setUpdatedAt(LocalDateTime.now());

        Driver updatedDriver = driverRepository.save(driver);
        DriverResponseDTO driverResponseDTO = convertToDto(updatedDriver);
        log.info("Driver profile updated successfully for ID: {}", id);

        return driverResponseDTO;
    }

    public DriverResponseDTO findUserById(UUID userId) {
        log.info("Attempting to find driver by ID: {}", userId);
        Driver userEntity = driverRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Driver not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID : " + userId);
                });
        log.info("Found driver with ID: {}", userId);
        return convertToDto(userEntity);
    }

    public static List<Driver> searchDriverRideHistory(String keyword) {
        log.info("Searching driver ride history with keyword: {}", keyword);
        // NOTE: The original method references a static search method on the repository,
        // which is unconventional for Spring Data JPA. Assuming DriverRepository is the interface
        // and searchDriverRideHistory is a custom query method.
        // If this method is not static in the actual repository, this will cause a compilation error.
        // Leaving it as is, but adding a note.
        // Assuming the repository has a method like: List<Driver> findByDriverDetails_LicenseNumberContaining(String keyword);
        // For now, retaining the original implementation:
        return DriverRepository.searchDriverRideHistory(keyword);
    }

    public DriverAllDetailsResponseDTO getDriverDashboardData(UUID driverId) {
        log.info("Fetching dashboard data for driver ID: {}", driverId);
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.error("Driver not found when generating dashboard data for ID: {}", driverId);
                    return new ResourceNotFoundException("Driver not found with ID: " + driverId);
                });

        // --- Setup Time Boundaries ---
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        log.debug("Calculating daily metrics since: {}", startOfToday);

        // 1. EARNINGS & RIDES
        Double todayEarnings = paymentRepository.sumEarningsByDriverSince(driverId, startOfToday);
        Double totalLifetimeEarnings = paymentRepository.sumTotalEarningsByDriver(driverId);
        Long todayRidesCompleted = rideRepository.countCompletedRidesByDriverSince(driverId, startOfToday);
        log.debug("Today's Earnings: {}, Today's Rides: {}", todayEarnings, todayRidesCompleted);

        // 2. RATING
        Double averageRating = ratingRepository.findAverageRatingByDriverId(driverId)
                .orElse(0.0);
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;
        log.debug("Average Rating: {}", roundedRating);


        // 3. ACCEPTANCE RATE
        Long totalAcceptedRides = rideRepository.countByDriverId(driverId);
        Long totalAssignedRides = rideRepository.countAssignedRidesByDriver(driverId);

        // Check for division by zero
        double acceptanceRate = (totalAssignedRides != null && totalAssignedRides > 0) ?
                (double) totalAcceptedRides / totalAssignedRides : 0.0;

        String acceptanceRateValue = String.format("%.0f%%", acceptanceRate * 100);
        log.debug("Acceptance Rate (Accepted/Assigned): {}/{} = {}", totalAcceptedRides, totalAssignedRides, acceptanceRateValue);

        // --- Build and Return DTO ---
        log.info("Successfully compiled dashboard data for driver ID: {}", driverId);
        return DriverAllDetailsResponseDTO.builder()
                .driverId(driverId)

                // Card Metrics
                .todaysEarnings(todayEarnings != null ? todayEarnings : 0.0)
                .todaysRides(todayRidesCompleted != null ? todayRidesCompleted.intValue() : 0)
                .driverRating(roundedRating)
                .additionalMetricLabel("Acceptance Rate")
                .additionalMetricValue(acceptanceRateValue)

                // Weekly Goals (Using Lifetime Earnings/Total Rides as placeholder for weekly achieved)
                .weeklyEarningsGoal(7500.0)
                .weeklyEarningsAchieved(totalLifetimeEarnings)
                .weeklyRidesGoal(50)
                .weeklyRidesAchieved(totalAcceptedRides != null ? totalAcceptedRides.intValue() : 0)
                .ratingMaintenanceGoal(4.5)
                .ratingMaintenanceAchieved(roundedRating)
                .acceptanceRateGoal(0.85)
                .acceptanceRateAchieved(acceptanceRate)
                .build();
    }
}