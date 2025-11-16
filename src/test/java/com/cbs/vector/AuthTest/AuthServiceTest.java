package com.cbs.vector.AuthTest;

import com.cbs.vector.dto.*;
import com.cbs.vector.model.*;
import com.cbs.vector.model.enums.UserRole;
import com.cbs.vector.repository.DriverRepository;
import com.cbs.vector.repository.UserRepository;
import com.cbs.vector.service.AuthService;
import com.cbs.vector.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DriverRepository driverRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterRider_Success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setEmail("john@example.com");
        dto.setPhone("1234567890");
        dto.setPasswordHash("password");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");
        user.setPhone("1234567890");
        user.setPasswordHash("hashedPassword");
        user.setRole(UserRole.RIDER);
        user.setCreatedAt(LocalDateTime.now());

        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        RiderRegistrationResponseDTO response = authService.registerRider(dto);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginUser_Success() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john@example.com", "password", UserRole.RIDER);
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByEmailAndRole("john@example.com", UserRole.RIDER)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("hashedPassword");
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);

        UserDetails result = authService.loginUser(loginDTO);

        assertNotNull(result);
        verify(userDetailsService, times(1)).loadUserByEmailAndRole("john@example.com", UserRole.RIDER);
    }

    @Test
    void testLoginUser_InvalidPassword() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("john@example.com", "wrongPassword", UserRole.RIDER);
        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByEmailAndRole("john@example.com", UserRole.RIDER)).thenReturn(userDetails);
        when(userDetails.getPassword()).thenReturn("hashedPassword");
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.loginUser(loginDTO));
    }

    @Test
    void testLoginUser_UserNotFound() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("notfound@example.com", "password", UserRole.RIDER);

        when(userDetailsService.loadUserByEmailAndRole("notfound@example.com", UserRole.RIDER))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class,
                () -> authService.loginUser(loginDTO));
    }

    @Test
    void testRegisterDriver_Success() {
        // Arrange
        DriverRegistrationDTO registrationDTO = createValidDriverRegistrationDTO();
        Driver savedDriver = createMockDriver();

        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        DriverResponseDTO response = authService.registerDriver(registrationDTO);

        // Assert
        assertNotNull(response);
        assertEquals(savedDriver.getId(), response.getId());
        assertEquals(savedDriver.getName(), response.getName());
        assertEquals(savedDriver.getPersonalDetails().getEmail(), response.getPersonalDetails().getEmail());
        verify(driverRepository, times(1)).save(any(Driver.class));
        verify(passwordEncoder, times(1)).encode(anyString());
    }

    @Test
    void testRegisterDriver_WithAllDetails() {
        // Arrange
        DriverRegistrationDTO registrationDTO = createValidDriverRegistrationDTO();
        Driver savedDriver = createMockDriver();

        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(driverRepository.save(any(Driver.class))).thenReturn(savedDriver);

        // Act
        DriverResponseDTO response = authService.registerDriver(registrationDTO);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getPersonalDetails());
        assertNotNull(response.getDriverDetails());
        assertNotNull(response.getVehicleDetails());
        assertNotNull(response.getBankingDetails());
        assertEquals("John", response.getPersonalDetails().getFirstName());
        assertEquals("Doe", response.getPersonalDetails().getLastName());
        assertEquals("john.doe@example.com", response.getPersonalDetails().getEmail());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    void testConvertToDto_Success() {
        // Arrange
        Driver driver = createMockDriver();

        // Act
        DriverResponseDTO dto = authService.convertToDto(driver);

        // Assert
        assertNotNull(dto);
        assertEquals(driver.getId(), dto.getId());
        assertEquals(driver.getDriverRole(), dto.getRole());
        assertEquals(driver.getStatus(), dto.getStatus());
        assertEquals(driver.getName(), dto.getName());
        assertEquals(driver.getPersonalDetails().getFirstName(), dto.getPersonalDetails().getFirstName());
        assertEquals(driver.getPersonalDetails().getLastName(), dto.getPersonalDetails().getLastName());
        assertEquals(driver.getPersonalDetails().getEmail(), dto.getPersonalDetails().getEmail());
        assertEquals(driver.getDriverDetails().getLicenseNumber(), dto.getDriverDetails().getLicenseNumber());
        assertEquals(driver.getVehicleDetails().getVehicleNumber(), dto.getVehicleDetails().getVehicleNumber());
        assertEquals(driver.getBankingDetails().getBankAccount(), dto.getBankingDetails().getBankAccount());
    }

    @Test
    void testConvertToDto_WithTimestamps() {
        // Arrange
        Driver driver = createMockDriver();
        LocalDateTime now = LocalDateTime.now();
        driver.setCreatedAt(now);
        driver.setUpdatedAt(now);

        // Act
        DriverResponseDTO dto = authService.convertToDto(driver);

        // Assert
        assertNotNull(dto);
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }

    // Helper method to create valid DriverRegistrationDTO
    private DriverRegistrationDTO createValidDriverRegistrationDTO() {
        DriverRegistrationDTO dto = new DriverRegistrationDTO();

        PersonalDetailsDTO personalDetails = new PersonalDetailsDTO();
        personalDetails.setFirstName("John");
        personalDetails.setLastName("Doe");
        personalDetails.setEmail("john.doe@example.com");
        personalDetails.setPhone("1234567890");
        personalDetails.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        personalDetails.setPassword("password123");
        personalDetails.setConfirmPassword("password123");
        dto.setPersonalDetails(personalDetails);

        DriverDetailsDTO driverDetails = new DriverDetailsDTO();
        driverDetails.setLicenseNumber("DL123456");
        driverDetails.setLicenseExpiry(java.time.LocalDate.of(2025, 12, 31));
        driverDetails.setExperience(5);
        driverDetails.setEmergencyName("Jane Doe");
        driverDetails.setEmergencyPhone("0987654321");
        driverDetails.setEmergencyRelation("Spouse");
        dto.setDriverDetails(driverDetails);

        VehicleDetailsDTO vehicleDetails = new VehicleDetailsDTO();
        vehicleDetails.setVehicleNumber("ABC123");
        vehicleDetails.setVehicleMake("Toyota");
        vehicleDetails.setVehicleModel("Camry");
        vehicleDetails.setVehicleYear(2020);
        vehicleDetails.setVehicleColor("Blue");
        dto.setVehicleDetails(vehicleDetails);

        BankingDetailsDTO bankingDetails = new BankingDetailsDTO();
        bankingDetails.setBankAccount("1234567890");
        bankingDetails.setRoutingNumber("987654321");
        dto.setBankingDetails(bankingDetails);

        return dto;
    }

    // Helper method to create mock Driver
    private Driver createMockDriver() {
        Driver driver = new Driver();
        driver.setId(UUID.randomUUID());
        driver.setName("John Doe");
        driver.setDriverRole(com.cbs.vector.model.DriverRole.DRIVER);
        driver.setStatus(com.cbs.vector.model.DriverStatus.UNAVAILABLE);
        driver.setCreatedAt(LocalDateTime.now());
        driver.setUpdatedAt(LocalDateTime.now());

        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName("John");
        personalDetails.setLastName("Doe");
        personalDetails.setEmail("john.doe@example.com");
        personalDetails.setPhone("1234567890");
        personalDetails.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
        personalDetails.setPassword("hashedPassword");
        driver.setPersonalDetails(personalDetails);

        DriverDetails driverDetails = new DriverDetails();
        driverDetails.setLicenseNumber("DL123456");
        driverDetails.setLicenseExpiry(java.time.LocalDate.of(2025, 12, 31));
        driverDetails.setExperience(5);
        driverDetails.setEmergencyName("Jane Doe");
        driverDetails.setEmergencyPhone("0987654321");
        driverDetails.setEmergencyRelation("Spouse");
        driver.setDriverDetails(driverDetails);

        VehicleDetails vehicleDetails = new VehicleDetails();
        vehicleDetails.setVehicleNumber("ABC123");
        vehicleDetails.setVehicleMake("Toyota");
        vehicleDetails.setVehicleModel("Camry");
        vehicleDetails.setVehicleYear(2020);
        vehicleDetails.setVehicleColor("Blue");
        driver.setVehicleDetails(vehicleDetails);

        BankingDetails bankingDetails = new BankingDetails();
        bankingDetails.setBankAccount("1234567890");
        bankingDetails.setRoutingNumber("987654321");
        driver.setBankingDetails(bankingDetails);

        return driver;
    }

}
