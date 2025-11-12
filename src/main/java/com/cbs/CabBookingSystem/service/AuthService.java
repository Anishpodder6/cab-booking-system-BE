package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.*;
import com.cbs.CabBookingSystem.model.*;
import com.cbs.CabBookingSystem.model.enums.UserRole;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import com.cbs.CabBookingSystem.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
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
        log.info("AuthService initialized.");
    }

    public RiderRegistrationResponseDTO registerRider(UserRegistrationDto userRegistrationDto) {
        log.info("Attempting to register new rider with email: {}", userRegistrationDto.getEmail());

        User rider = new User();
        rider.setFirstName(userRegistrationDto.getFirstName());
        rider.setLastName(userRegistrationDto.getLastName());
        rider.setEmail(userRegistrationDto.getEmail());
        rider.setPhone(userRegistrationDto.getPhone());
        rider.setPasswordHash(passwordEncoder.encode(userRegistrationDto.getPasswordHash()));
        rider.setRole(UserRole.RIDER);
        rider.setCreatedAt(LocalDateTime.now());

        User user = userRepository.save(rider);
        log.info("Rider successfully registered with ID: {}", user.getUserId());
        log.debug("Registered Rider Details: {}", user);
        return new RiderRegistrationResponseDTO(user);
    }

    public UserDetails loginUser(LoginRequestDTO loginDTO) {
        log.info("Attempting to log in user with email: {} and role: {}", loginDTO.email(), loginDTO.userRole());
        try {
            // 1. Explicitly load user by email and role
            UserDetails userDetails = userDetailsService.loadUserByEmailAndRole(loginDTO.email(), loginDTO.userRole());
            log.debug("User details loaded for email: {}", loginDTO.email());

            // 2. Manual BCrypt password check
            if (passwordEncoder.matches(loginDTO.passwordHash(), userDetails.getPassword())) {
                log.info("User login successful for email: {}", loginDTO.email());
                return userDetails;
            } else {
                log.warn("Login failed for email: {}. Invalid password provided.", loginDTO.email());
                throw new IllegalArgumentException("Invalid password.");
            }
        } catch (UsernameNotFoundException e) {
            log.error("Login failed for email: {}. User not found.", loginDTO.email());
            throw e;
        } catch (Exception e) {
            // Catches "Invalid password" and general exceptions
            log.error("Login failed for email: {}. Invalid credentials or unexpected error: {}", loginDTO.email(), e.getMessage());
            throw new IllegalArgumentException("Invalid credentials.");
        }
    }



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

        // Hash password
        String hashedPassword = passwordEncoder.encode(dto.getPersonalDetails().getPassword());
        pd.setPassword(hashedPassword);
        // Note: The ConfirmPassword field in the DTO is typically for validation,
        // but it is being encoded and set here for structural completeness based on the original code.
        pd.setConfirmPassword(hashedPassword);
        driver.setPersonalDetails(pd);
        log.debug("Personal Details mapped and password hashed.");


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

        log.debug("Driver entity conversion complete.");
        return driver;
    }


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

        log.debug("Driver response DTO conversion complete.");
        return dto;
    }

     //POST /api/drivers/register
    public DriverResponseDTO registerDriver(DriverRegistrationDTO registrationDTO) {
        log.info("Attempting to register new driver with email: {}", registrationDTO.getPersonalDetails().getEmail());

        Driver driver = convertToEntity(registrationDTO);
        Driver savedDriver = driverRepository.save(driver);

        log.info("Driver successfully registered with ID: {}", savedDriver.getId());
        log.debug("Saved Driver Details: {}", savedDriver);

        return convertToDto(savedDriver);
    }
}