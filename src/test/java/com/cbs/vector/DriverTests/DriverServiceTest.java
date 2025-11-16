package com.cbs.vector.DriverTests;

import com.cbs.vector.dto.*;
import com.cbs.vector.exception.ResourceNotFoundException;
import com.cbs.vector.model.*;
import com.cbs.vector.repository.DriverRepository;
import com.cbs.vector.service.DriverService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    // Helper to build a Driver entity with all embedded objects
    private Driver buildDriver(UUID id,
                               String firstName,
                               String lastName,
                               String email,
                               DriverStatus status) {
        Driver d = new Driver();
        d.setId(id);
        // Personal
        PersonalDetails pd = new PersonalDetails();
        pd.setFirstName(firstName);
        pd.setLastName(lastName);
        pd.setEmail(email);
        pd.setPhone("1234567890");
        pd.setDateOfBirth(LocalDate.of(1990, 1, 1));
        pd.setPassword("pwd");
        pd.setConfirmPassword("pwd");
        d.setPersonalDetails(pd);
        // Driver details
        DriverDetails dd = new DriverDetails();
        dd.setLicenseNumber("LIC123");
        dd.setLicenseExpiry(LocalDate.now().plusYears(1));
        dd.setExperience(5);
        dd.setEmergencyName("EMG NAME");
        dd.setEmergencyPhone("9999999999");
        dd.setEmergencyRelation("Friend");
        d.setDriverDetails(dd);
        // Vehicle
        VehicleDetails vd = new VehicleDetails();
        vd.setVehicleNumber("VH-001");
        vd.setVehicleMake("Make");
        vd.setVehicleModel("Model");
        vd.setVehicleYear(2020);
        vd.setVehicleColor("Black");
        d.setVehicleDetails(vd);
        // Banking
        BankingDetails bd = new BankingDetails();
        bd.setBankAccount("ACC123");
        bd.setRoutingNumber("ROUT987");
        d.setBankingDetails(bd);

        d.setStatus(status);
        d.setDriverRole(DriverRole.DRIVER);
        d.setCreatedAt(LocalDateTime.now());
        d.setUpdatedAt(LocalDateTime.now());
        d.setName(firstName + " " + lastName);
        return d;
    }

    @Test
    @DisplayName("registerDriver maps and saves successfully")
    void testRegisterDriver_success() {
        DriverRegistrationDTO reg = new DriverRegistrationDTO();

        PersonalDetailsDTO pdDTO = new PersonalDetailsDTO();
        pdDTO.setFirstName("John");
        pdDTO.setLastName("Doe");
        pdDTO.setEmail("john@ex.com");
        pdDTO.setPhone("1112223333");
        pdDTO.setDateOfBirth(LocalDate.of(1992, 5, 10));
        pdDTO.setPassword("pass");
        pdDTO.setConfirmPassword("pass");
        reg.setPersonalDetails(pdDTO);

        DriverDetailsDTO ddDTO = new DriverDetailsDTO();
        ddDTO.setLicenseNumber("LIC999");
        ddDTO.setLicenseExpiry(LocalDate.now().plusYears(2));
        ddDTO.setExperience(7);
        ddDTO.setEmergencyName("Jane");
        ddDTO.setEmergencyPhone("7778889999");
        ddDTO.setEmergencyRelation("Sister");
        reg.setDriverDetails(ddDTO);

        VehicleDetailsDTO vdDTO = new VehicleDetailsDTO();
        vdDTO.setVehicleNumber("CAR123");
        vdDTO.setVehicleMake("Toyota");
        vdDTO.setVehicleModel("Prius");
        vdDTO.setVehicleYear(2021);
        vdDTO.setVehicleColor("Blue");
        reg.setVehicleDetails(vdDTO);

        BankingDetailsDTO bdDTO = new BankingDetailsDTO();
        bdDTO.setBankAccount("BANK001");
        bdDTO.setRoutingNumber("ROUTE001");
        reg.setBankingDetails(bdDTO);

        Driver saved = buildDriver(UUID.randomUUID(), "John", "Doe", "john@ex.com", DriverStatus.UNAVAILABLE);
        when(driverRepository.save(any(Driver.class))).thenReturn(saved);

        DriverResponseDTO resp = driverService.registerDriver(reg);

        assertNotNull(resp.getId());
        assertEquals("John Doe", resp.getName());
        assertEquals(DriverStatus.UNAVAILABLE, resp.getStatus());
        assertEquals("John", resp.getPersonalDetails().getFirstName());
        verify(driverRepository, times(1)).save(any(Driver.class));
    }

    @Test
    @DisplayName("getAvailableDrivers returns mapped DTO list")
    void testGetAvailableDrivers_returnsMappedDTOs() {
        Driver d1 = buildDriver(UUID.randomUUID(), "A", "One", "a@ex.com", DriverStatus.AVAILABLE);
        Driver d2 = buildDriver(UUID.randomUUID(), "B", "Two", "b@ex.com", DriverStatus.AVAILABLE);
        when(driverRepository.findByStatus(DriverStatus.AVAILABLE)).thenReturn(List.of(d1, d2));

        List<DriverResponseDTO> list = driverService.getAvailableDrivers();

        assertEquals(2, list.size());
        assertEquals("A One", list.get(0).getName());
        assertEquals(DriverStatus.AVAILABLE, list.get(1).getStatus());
        verify(driverRepository, times(1)).findByStatus(DriverStatus.AVAILABLE);
    }

    @Test
    @DisplayName("updateDriverStatus updates when driver exists")
    void testUpdateDriverStatus_driverFound() {
        UUID id = UUID.randomUUID();
        Driver d = buildDriver(id, "John", "Doe", "john@ex.com", DriverStatus.UNAVAILABLE);
        when(driverRepository.findById(id)).thenReturn(Optional.of(d));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<DriverResponseDTO> result = driverService.updateDriverStatus(id, DriverStatus.AVAILABLE);

        assertTrue(result.isPresent());
        assertEquals(DriverStatus.AVAILABLE, result.get().getStatus());
        verify(driverRepository).findById(id);
        verify(driverRepository).save(d);
    }

    @Test
    @DisplayName("updateDriverStatus returns empty when not found")
    void testUpdateDriverStatus_driverNotFound() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        Optional<DriverResponseDTO> result = driverService.updateDriverStatus(id, DriverStatus.AVAILABLE);

        assertTrue(result.isEmpty());
        verify(driverRepository).findById(id);
        verify(driverRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateDriverProfile updates fields correctly")
    void testUpdateDriverProfile_success() {
        UUID id = UUID.randomUUID();
        Driver existing = buildDriver(id, "Old", "Name", "old@ex.com", DriverStatus.UNAVAILABLE);
        when(driverRepository.findById(id)).thenReturn(Optional.of(existing));
        when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

        DriverUpdateDTO upd = new DriverUpdateDTO();
        upd.setFirstName("New");
        upd.setLastName("Driver");
        upd.setPhone("5551234567");
        upd.setLicenseNumber("NEWLIC");
        upd.setVehicleColor("Red");
        upd.setStatus(DriverStatus.AVAILABLE);

        DriverResponseDTO resp = driverService.updateDriverProfile(id, upd);

        assertNotNull(resp);
        assertEquals("Old Name", resp.getName());
        assertEquals("5551234567", resp.getPersonalDetails().getPhone());
        assertEquals("NEWLIC", resp.getDriverDetails().getLicenseNumber());
        assertEquals("Red", resp.getVehicleDetails().getVehicleColor());
        assertEquals(DriverStatus.AVAILABLE, resp.getStatus());
        verify(driverRepository).save(existing);
    }


    @Test
    @DisplayName("updateDriverProfile throws ResourceNotFoundException when driver not found")
    void testUpdateDriverProfile_driverNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        DriverUpdateDTO updateDTO = new DriverUpdateDTO();
        // Mock the repository call to return empty
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        // Use assertThrows to check if the specific exception is thrown
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class, // 1. The expected type of exception
                () -> driverService.updateDriverProfile(id, updateDTO), // 2. The code that should throw it
                "Expected ResourceNotFoundException to be thrown, but it wasn't."
        );

        // Optional: Verify the exception message
        assertTrue(thrown.getMessage().contains(id.toString()));

        // Verify that findById was called
        verify(driverRepository).findById(id);

        // Ensure save was NEVER called since the driver wasn't found
        verify(driverRepository, never()).save(any(Driver.class));
    }

    @Test
    @DisplayName("findUserById returns DTO when found")
    void testFindUserById_success() {
        UUID id = UUID.randomUUID();
        Driver d = buildDriver(id, "Jane", "Smith", "jane@ex.com", DriverStatus.AVAILABLE);
        when(driverRepository.findById(id)).thenReturn(Optional.of(d));

        DriverResponseDTO dto = driverService.findUserById(id);

        assertEquals(id, dto.getId());
        assertEquals("Jane Smith", dto.getName());
        verify(driverRepository).findById(id);
    }

    @Test
    @DisplayName("findUserById throws when not found")
    void testFindUserById_notFound() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> driverService.findUserById(id));
        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("searchDriverRideHistory returns list from static repository method")
    void testSearchDriverRideHistory_returnsResults() {
        Driver d1 = buildDriver(UUID.randomUUID(), "Alpha", "Tester", "a@ex.com", DriverStatus.AVAILABLE);
        Driver d2 = buildDriver(UUID.randomUUID(), "Beta", "Tester", "b@ex.com", DriverStatus.AVAILABLE);

        try (MockedStatic<DriverRepository> mocked = mockStatic(DriverRepository.class)) {
            mocked.when(() -> DriverRepository.searchDriverRideHistory("test"))
                    .thenReturn(List.of(d1, d2));

            List<Driver> result = DriverService.searchDriverRideHistory("test");
            assertEquals(2, result.size());
            assertEquals("Alpha", result.get(0).getPersonalDetails().getFirstName());
        }
    }

    @Test
    @DisplayName("searchDriverRideHistory returns empty list when static returns empty")
    void testSearchDriverRideHistory_empty() {
        try (MockedStatic<DriverRepository> mocked = mockStatic(DriverRepository.class)) {
            mocked.when(() -> DriverRepository.searchDriverRideHistory("none"))
                    .thenReturn(Collections.emptyList());

            List<Driver> result = DriverService.searchDriverRideHistory("none");
            assertTrue(result.isEmpty());
        }
    }
}