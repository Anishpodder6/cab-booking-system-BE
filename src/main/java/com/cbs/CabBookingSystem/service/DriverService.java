package com.cbs.CabBookingSystem.service;
import com.cbs.CabBookingSystem.repository.PaymentRepository;
import com.cbs.CabBookingSystem.repository.RatingRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
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
        Driver driver = new Driver();

        // Map Personal Details
        PersonalDetails pd = new PersonalDetails();
        pd.setFirstName(dto.getPersonalDetails().getFirstName());
        pd.setLastName(dto.getPersonalDetails().getLastName());
        pd.setEmail(dto.getPersonalDetails().getEmail());
        pd.setPhone(dto.getPersonalDetails().getPhone());
        pd.setDateOfBirth(dto.getPersonalDetails().getDateOfBirth());
        // Note: Passwords stored in plain text for this exercise only.
        // Use BCrypt in production!
        pd.setPassword(dto.getPersonalDetails().getPassword());
        pd.setConfirmPassword(dto.getPersonalDetails().getConfirmPassword());
        driver.setPersonalDetails(pd);

        // Map Driver Details
        DriverDetails dd = new DriverDetails();
        dd.setLicenseNumber(dto.getDriverDetails().getLicenseNumber());
        dd.setLicenseExpiry(dto.getDriverDetails().getLicenseExpiry());
        dd.setExperience(dto.getDriverDetails().getExperience());
        dd.setEmergencyName(dto.getDriverDetails().getEmergencyName());
        dd.setEmergencyPhone(dto.getDriverDetails().getEmergencyPhone());
        dd.setEmergencyRelation(dto.getDriverDetails().getEmergencyRelation());
        driver.setDriverDetails(dd);

        // Map Vehicle Details
        VehicleDetails vd = new VehicleDetails();
        vd.setVehicleNumber(dto.getVehicleDetails().getVehicleNumber());
        vd.setVehicleMake(dto.getVehicleDetails().getVehicleMake());
        vd.setVehicleModel(dto.getVehicleDetails().getVehicleModel());
        vd.setVehicleYear(dto.getVehicleDetails().getVehicleYear());
        vd.setVehicleColor(dto.getVehicleDetails().getVehicleColor());
        driver.setVehicleDetails(vd);

        // Map Banking Details
        BankingDetails bd = new BankingDetails();
        bd.setBankAccount(dto.getBankingDetails().getBankAccount());
        bd.setRoutingNumber(dto.getBankingDetails().getRoutingNumber());
        driver.setBankingDetails(bd);

        // Role and Status default set in Driver entity constructor via Lombok/JPA annotations
        return driver;
    }

    /**
     * Converts a Driver Entity (JPA/Database) to a DriverResponseDTO (API Response).
     * @param driver The entity retrieved from the database.
     * @return The response DTO sent back to the controller (excluding sensitive data like raw password).
     */
    public DriverResponseDTO convertToDto(Driver driver) {
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
        Driver driver = convertToEntity(registrationDTO);
        Driver savedDriver = driverRepository.save(driver);
        return convertToDto(savedDriver);
    }

    /**
     * 3. GET /api/drivers/available
     * Finds all drivers with the AVAILABLE status and returns them as a list of DTOs.
     */
    public List<DriverResponseDTO> getAvailableDrivers() {
        return driverRepository.findByStatus(DriverStatus.AVAILABLE).stream()
                .map(this::convertToDto) // Convert each entity to a DTO
                .collect(Collectors.toList());
    }

    /**
     * 4. PUT /api/drivers/status/{id}
     * Finds driver by ID, updates status, saves it, and returns the updated DTO.
     */
    public Optional<DriverResponseDTO> updateDriverStatus(UUID id, DriverStatus newStatus) {
        return driverRepository.findById(id).map(driver -> {
            driver.setStatus(newStatus);
            Driver updatedDriver = driverRepository.save(driver);
            return convertToDto(updatedDriver); // Convert updated Entity to DTO
        });
    }

    // FOR PUT in Drivers Profile
    @Transactional
    public Optional<DriverResponseDTO> updateDriverProfile(UUID id, DriverUpdateDTO updateDTO) {

        Optional<Driver> driverOpt = driverRepository.findById(id);

        if (driverOpt.isEmpty()) {
            return Optional.empty(); // Driver not found
        }

        Driver driver = driverOpt.get();

        // --- 1. Update Personal Details ---
        PersonalDetails pd = driver.getPersonalDetails();

        if (updateDTO.getFirstName() != null) pd.setFirstName(updateDTO.getFirstName());
        if (updateDTO.getLastName() != null) pd.setLastName(updateDTO.getLastName());
        driver.setName(updateDTO.getFirstName() + " " + updateDTO.getLastName());
        if (updateDTO.getPhone() != null) pd.setPhone(updateDTO.getPhone());
        if (updateDTO.getDateOfBirth() != null) pd.setDateOfBirth(updateDTO.getDateOfBirth());
        driver.setPersonalDetails(pd);

        // --- 2. Update Driver Details ---
        DriverDetails dd = driver.getDriverDetails();
        if (updateDTO.getLicenseNumber() != null) dd.setLicenseNumber(updateDTO.getLicenseNumber());
        if (updateDTO.getLicenseExpiry() != null) dd.setLicenseExpiry(updateDTO.getLicenseExpiry());
        if (updateDTO.getExperience() != null) dd.setExperience(updateDTO.getExperience());
        if (updateDTO.getEmergencyName() != null) dd.setEmergencyName(updateDTO.getEmergencyName());
        if (updateDTO.getEmergencyPhone() != null) dd.setEmergencyPhone(updateDTO.getEmergencyPhone());
        if (updateDTO.getEmergencyRelation() != null) dd.setEmergencyRelation(updateDTO.getEmergencyRelation());
        driver.setDriverDetails(dd);

        // --- 3. Update Vehicle Details ---
        VehicleDetails vd = driver.getVehicleDetails();
        if (updateDTO.getVehicleNumber() != null) vd.setVehicleNumber(updateDTO.getVehicleNumber());
        if (updateDTO.getVehicleMake() != null) vd.setVehicleMake(updateDTO.getVehicleMake());
        if (updateDTO.getVehicleModel() != null) vd.setVehicleModel(updateDTO.getVehicleModel());
        if (updateDTO.getVehicleYear() != null) vd.setVehicleYear(updateDTO.getVehicleYear());
        if (updateDTO.getVehicleColor() != null) vd.setVehicleColor(updateDTO.getVehicleColor());
        driver.setVehicleDetails(vd);

        // --- 4. Update Banking Details ---
        BankingDetails bd = driver.getBankingDetails();
        if (updateDTO.getBankAccount() != null) bd.setBankAccount(updateDTO.getBankAccount());
        if (updateDTO.getRoutingNumber() != null) bd.setRoutingNumber(updateDTO.getRoutingNumber());
        driver.setBankingDetails(bd);

        // --- 5. Update Status (Optional) ---
        if (updateDTO.getStatus() != null) {
            driver.setStatus(updateDTO.getStatus());
        }

        // The @PreUpdate method in the Driver entity handles the 'updatedAt' timestamp and 'name'
        Driver updatedDriver = driverRepository.save(driver);

        return Optional.of(convertToDto(updatedDriver));
    }

    public DriverResponseDTO findUserById(UUID userId) {
        Driver userEntity = driverRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + userId));
        return convertToDto(userEntity);
    }

    public static List<Driver> searchDriverRideHistory(String keyword) {
        return DriverRepository.searchDriverRideHistory(keyword);
    }

    public DriverAllDetailsResponseDTO getDriverDashboardData(UUID driverId) {

        // --- Setup Time Boundaries ---
        // Get the start of the current day (00:00:00) for daily metrics
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();

        // 1. EARNINGS & RIDES
        Double todayEarnings = paymentRepository.sumEarningsByDriverSince(driverId, startOfToday);
        Double totalLifetimeEarnings = paymentRepository.sumTotalEarningsByDriver(driverId); // For potential use in weekly goals

        Long todayRidesCompleted = rideRepository.countCompletedRidesByDriverSince(driverId, startOfToday);

        // 2. RATING
        Double averageRating = ratingRepository.findAverageRatingByDriverId(driverId)
                .orElse(0.0);
        double roundedRating = Math.round(averageRating * 10.0) / 10.0;


        // 3. ACCEPTANCE RATE (Only possible if you log assigned/declined attempts. We calculate Accepted/Assigned total)
        Long totalAcceptedRides = rideRepository.countByDriverId(driverId); // Counts total rides linked to driverId
        Long totalAssignedRides = rideRepository.countAssignedRidesByDriver(driverId); // Counts attempts

        // Check for division by zero
        double acceptanceRate = (totalAssignedRides != null && totalAssignedRides > 0) ?
                (double) totalAcceptedRides / totalAssignedRides : 0.0;

        String acceptanceRateValue = String.format("%.0f%%", acceptanceRate * 100);

        // --- Build and Return DTO ---
        return DriverAllDetailsResponseDTO.builder()
                .driverId(driverId)

                // Card Metrics
                .todaysEarnings(todayEarnings != null ? todayEarnings : 0.0)
                .todaysRides(todayRidesCompleted != null ? todayRidesCompleted.intValue() : 0)
                .driverRating(roundedRating)
                .additionalMetricLabel("Acceptance Rate")
                .additionalMetricValue(acceptanceRateValue)

                // Weekly Goals (Goal values are hardcoded as they are administrative targets,
                // but achieved values are now real)
                .weeklyEarningsGoal(500.0) // Goal is an administrative target
                .weeklyEarningsAchieved(totalLifetimeEarnings) // Using Lifetime Earnings as a placeholder for a weekly metric
                .weeklyRidesGoal(50)
                .weeklyRidesAchieved(totalAcceptedRides != null ? totalAcceptedRides.intValue() : 0) // Total rides achieved
                .ratingMaintenanceGoal(4.5)
                .ratingMaintenanceAchieved(roundedRating)
                .acceptanceRateGoal(0.85)
                .acceptanceRateAchieved(acceptanceRate)
                .build();
    }
}