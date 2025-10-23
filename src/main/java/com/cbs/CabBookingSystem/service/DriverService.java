package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.*;
import com.cbs.CabBookingSystem.model.*;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

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
        dto.setRole(driver.getRole());
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

    // 4. POST /api/drivers/login (MODIFIED)
    public Optional<DriverResponseDTO> loginDriver(DriverLoginDTO loginDTO) {
        // 1. Find driver(s) by email - now returns a List
        List<Driver> drivers = driverRepository.findByPersonalDetailsEmail(loginDTO.getEmail());

        // Check if no driver was found
        if (drivers.isEmpty()) {
            return Optional.empty(); // No driver found
        }

        // 🚨 Critical Note on Duplicates:
        // We will take the first result, but the *real* fix is ensuring email uniqueness
        // in your Driver model (using @Column(unique = true)) and database.
        Driver driver = drivers.get(0);

        // 2. Basic Password Check
        if (driver.getPersonalDetails().getPassword().equals(loginDTO.getPassword())) {
            // Login successful
            return Optional.of(convertToDto(driver));
        } else {
            return Optional.empty(); // Password mismatch
        }
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
}