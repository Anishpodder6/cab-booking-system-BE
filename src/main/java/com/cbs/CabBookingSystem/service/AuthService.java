package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.*;
import com.cbs.CabBookingSystem.model.*;
import com.cbs.CabBookingSystem.model.enums.UserRole;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import com.cbs.CabBookingSystem.util.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {


    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public AuthService(UserRepository userRepository, DriverRepository driverRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    public RiderRegistrationResponseDTO registerRider(UserRegistrationDto userRegistrationDto) {
        User rider = new User();
        rider.setFirstName(userRegistrationDto.getFirstName());
        rider.setLastName(userRegistrationDto.getLastName());
        rider.setEmail(userRegistrationDto.getEmail());
        rider.setPhone(userRegistrationDto.getPhone());
        rider.setPasswordHash(passwordEncoder.encode(userRegistrationDto.getPasswordHash()));
        rider.setRole(UserRole.RIDER);
        rider.setCreatedAt(LocalDateTime.now());
        User user = userRepository.save(rider);
        return new RiderRegistrationResponseDTO(user);
    }

    public UserDetails loginUser(LoginRequestDTO loginDTO) {
        try {
            // 1. Explicitly load user by email and role
            UserDetails userDetails = userDetailsService.loadUserByEmailAndRole(loginDTO.email(), loginDTO.userRole());

            // 2. Manual BCrypt password check
            if (passwordEncoder.matches(loginDTO.passwordHash(), userDetails.getPassword())) {
                return userDetails;
            } else {
                throw new IllegalArgumentException("Invalid password.");
            }
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            // Catches "Invalid password"
            throw new IllegalArgumentException("Invalid credentials.");
        }
    }



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
        pd.setPassword(passwordEncoder.encode(dto.getPersonalDetails().getPassword()));
        pd.setConfirmPassword(passwordEncoder.encode(dto.getPersonalDetails().getConfirmPassword()));
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
}
