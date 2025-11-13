package com.cbs.CabBookingSystem.RideBookingTests;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.exception.customexception.AlreadyRideAssignedException;
import com.cbs.CabBookingSystem.exception.customexception.DriverNotFound;
import com.cbs.CabBookingSystem.exception.customexception.RideNotFound;
import com.cbs.CabBookingSystem.exception.customexception.UserNotFound;
import com.cbs.CabBookingSystem.model.Rating;
import com.cbs.CabBookingSystem.model.Ride;
import com.cbs.CabBookingSystem.model.RideWithRating;
import com.cbs.CabBookingSystem.model.enums.RideStatus;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import com.cbs.CabBookingSystem.service.RatingService;
import com.cbs.CabBookingSystem.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideServiceImplTest {

    @Mock
    private RideRepository rideRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private RatingService ratingService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride testRide;
    private RideDto testRideDto;
    private UUID testRideId;
    private UUID testUserId;
    private UUID testDriverId;

    @BeforeEach
    void setUp() {
        testRideId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testDriverId = UUID.randomUUID();

        testRide = new Ride();
        testRide.setRideId(testRideId);
        testRide.setUserId(testUserId);
        testRide.setPickupLocation("Test Pickup");
        testRide.setDropLocation("Test Drop");
        testRide.setFare(25.0);
        testRide.setStatus(RideStatus.LookingForDriver);

        testRideDto = new RideDto(
        testUserId,
        "Test Pickup",
        "Test Drop",
        null,
        "Sedan",
        25.0,
        RideStatus.LookingForDriver,
                "anish@gmail.com"
        );
    }

    // Helper method to create maps with null values
    private Map<String, Object> createMap(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, Object> createMap(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    @Test
    void addRide_ShouldCreateRide_WhenUserExists() {
        // Arrange
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // Create a new ride instance that will be returned by modelMapper
        Ride mappedRide = new Ride();
        mappedRide.setUserId(testUserId);
        mappedRide.setPickupLocation("Test Pickup");
        mappedRide.setDropLocation("Test Drop");
        mappedRide.setFare(25.0);
        mappedRide.setStatus(RideStatus.LookingForDriver);

        when(modelMapper.map(testRideDto, Ride.class)).thenReturn(mappedRide);
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride rideToSave = invocation.getArgument(0);
            // The service sets a random UUID, so we need to handle this
            if (rideToSave.getRideId() == null) {
                rideToSave.setRideId(testRideId); // Set our test UUID for consistency
            }
            return rideToSave;
        });

        // Act
        Ride result = rideService.addRide(testRideDto);

        // Assert - Don't check for specific UUID since it's randomly generated
        assertNotNull(result);
        assertNotNull(result.getRideId()); // Just verify that an ID was set
        assertEquals(testUserId, result.getUserId());
        assertEquals("Test Pickup", result.getPickupLocation());
        assertEquals("Test Drop", result.getDropLocation());
        assertEquals(25.0, result.getFare());
        assertEquals(RideStatus.LookingForDriver, result.getStatus());

        verify(userRepository, times(1)).existsById(testUserId);
        verify(rideRepository, times(1)).save(any(Ride.class));
    }

    @Test
    void addRide_ShouldThrowUserNotFound_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.existsById(testUserId)).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFound.class, () -> rideService.addRide(testRideDto));
        verify(userRepository, times(1)).existsById(testUserId);
        verify(rideRepository, never()).save(any(Ride.class));
    }

    @Test
    void getRideById_ShouldReturnRide_WhenValidId() {
        // Arrange
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));

        // Act
        Ride result = rideService.getRideById(testRideId);

        // Assert
        assertNotNull(result);
        assertEquals(testRideId, result.getRideId());
        verify(rideRepository, times(1)).findById(testRideId);
    }

    @Test
    void getRideById_ShouldThrowRideNotFound_WhenInvalidId() {
        // Arrange
        when(rideRepository.findById(testRideId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RideNotFound.class, () -> rideService.getRideById(testRideId));
        verify(rideRepository, times(1)).findById(testRideId);
    }

    @Test
    void patchRideData_ShouldAssignDriver_WhenValidDriverId() {
        // Arrange
        Map<String, Object> updateData = createMap("driverId", testDriverId.toString());
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(driverRepository.existsById(testDriverId)).thenReturn(true);
        when(rideRepository.existsByRideIdAndDriverIdIsNotNull(testRideId)).thenReturn(false);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        Ride result = rideService.patchRideData(updateData, testRideId);

        // Assert
        assertNotNull(result);
        verify(rideRepository, times(1)).findById(testRideId);
        verify(driverRepository, times(1)).existsById(testDriverId);
        verify(rideRepository, times(1)).save(testRide);
        verify(messagingTemplate, times(1)).convertAndSend("/topic/rides/" + testRideId, testRide);
    }

    @Test
    void patchRideData_ShouldUpdateStatus_WhenValidStatus() {
        // Arrange
        Map<String, Object> updateData = createMap("status", "ConfirmedByDriver");
        testRide.setDriverId(testDriverId);
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        Ride result = rideService.patchRideData(updateData, testRideId);

        // Assert
        assertNotNull(result);
        verify(rideRepository, times(1)).findById(testRideId);
        verify(rideRepository, times(1)).save(testRide);
    }

    @Test
    void patchRideData_ShouldThrowAlreadyRideAssigned_WhenDriverAlreadyAssigned() {
        // Arrange
        Map<String, Object> updateData = createMap("driverId", testDriverId.toString());
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(driverRepository.existsById(testDriverId)).thenReturn(true);
        when(rideRepository.existsByRideIdAndDriverIdIsNotNull(testRideId)).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyRideAssignedException.class,
        () -> rideService.patchRideData(updateData, testRideId));
    }

    @Test
    void patchRideData_ShouldThrowDriverNotFound_WhenInvalidDriverId() {
        // Arrange
        Map<String, Object> updateData = createMap("driverId", testDriverId.toString());
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(driverRepository.existsById(testDriverId)).thenReturn(false);

        // Act & Assert
        assertThrows(DriverNotFound.class, () -> rideService.patchRideData(updateData, testRideId));
    }

    @Test
    void patchRideData_ShouldSetDriverIdToNull_WhenNullValueProvided() {
        // Arrange
        Map<String, Object> updateData = createMap("driverId", null);
        testRide.setDriverId(testDriverId); // Set a driver initially
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        Ride result = rideService.patchRideData(updateData, testRideId);

        // Assert
        assertNotNull(result);
        verify(rideRepository, times(1)).findById(testRideId);
        verify(rideRepository, times(1)).save(testRide);
        // The driverId should be set to null in the service method
    }

    @Test
    void patchRideData_ShouldThrowException_WhenInvalidStatusProvided() {
        // Arrange
        Map<String, Object> updateData = createMap("status", "InvalidStatus");
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
        () -> rideService.patchRideData(updateData, testRideId));
    }

    @Test
    void patchRideData_ShouldThrowException_WhenInvalidDriverIdFormat() {
        // Arrange
        Map<String, Object> updateData = createMap("driverId", "invalid-uuid-format");
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
        () -> rideService.patchRideData(updateData, testRideId));
    }

    @Test
    void patchRideData_ShouldHandleMultipleUpdates() {
        // Arrange
        Map<String, Object> updateData = createMap(
        "driverId", testDriverId.toString(),
        "status", "ConfirmedByDriver"
        );
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));
        when(driverRepository.existsById(testDriverId)).thenReturn(true);
        when(rideRepository.existsByRideIdAndDriverIdIsNotNull(testRideId)).thenReturn(false);
        when(rideRepository.save(any(Ride.class))).thenReturn(testRide);

        // Act
        Ride result = rideService.patchRideData(updateData, testRideId);

        // Assert
        assertNotNull(result);
        verify(rideRepository, times(1)).findById(testRideId);
        verify(driverRepository, times(1)).existsById(testDriverId);
        verify(rideRepository, times(1)).save(testRide);
    }

    @Test
    void patchRideData_ShouldThrowException_WhenUnknownFieldProvided() {
        // Arrange
        Map<String, Object> updateData = createMap("unknownField", "someValue");
        when(rideRepository.findById(testRideId)).thenReturn(Optional.of(testRide));

        // Act & Assert
        assertThrows(IllegalStateException.class,
        () -> rideService.patchRideData(updateData, testRideId));
    }

    @Test
    void getRiderUpcomingRide_ShouldReturnRideList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findRiderUpcomingRides(testUserId)).thenReturn(rides);

        // Act
        List<Ride> result = rideService.getRiderUpcomingRide(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rideRepository, times(1)).findRiderUpcomingRides(testUserId);
    }

    @Test
    void getUnassignedRides_ShouldReturnRideList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findUnassignedRides()).thenReturn(rides);

        // Act
        List<Ride> result = rideService.getUnassignedRides();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rideRepository, times(1)).findUnassignedRides();
    }

    @Test
    void getDriverUpcomingRide_ShouldReturnRideList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findDriverUpcomingRides(testUserId)).thenReturn(rides);

        // Act
        List<Ride> result = rideService.getDriverUpcomingRide(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rideRepository, times(1)).findDriverUpcomingRides(testUserId);
    }

    @Test
    void getAllRidesForUser_ShouldReturnRideList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findAllByUserId(testUserId)).thenReturn(rides);

        // Act
        List<Ride> result = rideService.getAllRidesForUser(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(rideRepository, times(1)).findAllByUserId(testUserId);
    }

    @Test
    void hasTwoRides_ShouldReturnTrue_WhenTwoOrMoreActiveRides() {
        // Arrange
        when(rideRepository.countActiveRidesByUserId(testUserId)).thenReturn(2L);

        // Act
        Boolean result = rideService.hasTwoRides(testUserId);

        // Assert
        assertTrue(result);
        verify(rideRepository, times(1)).countActiveRidesByUserId(testUserId);
    }

    @Test
    void hasTwoRides_ShouldReturnFalse_WhenLessThanTwoActiveRides() {
        // Arrange
        when(rideRepository.countActiveRidesByUserId(testUserId)).thenReturn(1L);

        // Act
        Boolean result = rideService.hasTwoRides(testUserId);

        // Assert
        assertFalse(result);
        verify(rideRepository, times(1)).countActiveRidesByUserId(testUserId);
    }

    @Test
    void getRideStatus_ShouldReturnStatus() {
        // Arrange
        when(rideRepository.findStatusByRideId(testRideId)).thenReturn(RideStatus.ConfirmedByDriver);

        // Act
        RideStatus result = rideService.getRideStatus(testRideId);

        // Assert
        assertEquals(RideStatus.ConfirmedByDriver, result);
        verify(rideRepository, times(1)).findStatusByRideId(testRideId);
    }

    @Test
    void getRiderHistory_ShouldReturnRideWithRatingList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        RideWithRating rideWithRating = new RideWithRating();
        rideWithRating.setRideId(testRideId);
        Rating rating = new Rating();

        when(rideRepository.findAllByUserIdOrderByDateTimeDesc(testUserId)).thenReturn(rides);
        when(modelMapper.map(testRide, RideWithRating.class)).thenReturn(rideWithRating);
        when(ratingService.getRatingByRideId(testRideId)).thenReturn(rating);

        // Act
        List<RideWithRating> result = rideService.getRiderHistory(testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRideId, result.get(0).getRideId());
        assertEquals(rating, result.get(0).getRating());
        verify(rideRepository, times(1)).findAllByUserIdOrderByDateTimeDesc(testUserId);
        verify(ratingService, times(1)).getRatingByRideId(testRideId);
    }

    @Test
    void getDriverHistory_ShouldReturnRideWithRatingList() {
        // Arrange
        List<Ride> rides = List.of(testRide);
        RideWithRating rideWithRating = new RideWithRating();
        rideWithRating.setRideId(testRideId);
        Rating rating = new Rating();

        when(rideRepository.findByDriverIdOrderByDateTimeDesc(testDriverId)).thenReturn(rides);
        when(modelMapper.map(testRide, RideWithRating.class)).thenReturn(rideWithRating);
        when(ratingService.getRatingByRideId(testRideId)).thenReturn(rating);

        // Act
        List<RideWithRating> result = rideService.getDriverHistory(testDriverId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRideId, result.get(0).getRideId());
        assertEquals(rating, result.get(0).getRating());
        verify(rideRepository, times(1)).findByDriverIdOrderByDateTimeDesc(testDriverId);
        verify(ratingService, times(1)).getRatingByRideId(testRideId);
    }

    @Test
    void getCarTypeRideCountForRider_ShouldReturnCountMap() {
        // Arrange
        testRide.setCarType("Sedan");
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findAllByUserId(testUserId)).thenReturn(rides);

        // Act
        Map<String, Long> result = rideService.getCarTypeRideCountForRider(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("Sedan"));
        assertEquals(1L, result.get("Sedan"));
        verify(rideRepository, times(1)).findAllByUserId(testUserId);
    }

    @Test
    void getCarTypeRideCountForRider_ShouldFilterNullCarTypes() {
        // Arrange
        testRide.setCarType(null);
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findAllByUserId(testUserId)).thenReturn(rides);

        // Act
        Map<String, Long> result = rideService.getCarTypeRideCountForRider(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rideRepository, times(1)).findAllByUserId(testUserId);
    }

    @Test
    void getCarTypeRideCountForRider_ShouldFilterEmptyCarTypes() {
        // Arrange
        testRide.setCarType("");
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findAllByUserId(testUserId)).thenReturn(rides);

        // Act
        Map<String, Long> result = rideService.getCarTypeRideCountForRider(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rideRepository, times(1)).findAllByUserId(testUserId);
    }

    @Test
    void getCarTypeRideCountForRider_ShouldFilterBlankCarTypes() {
        // Arrange
        testRide.setCarType("   ");
        List<Ride> rides = List.of(testRide);
        when(rideRepository.findAllByUserId(testUserId)).thenReturn(rides);

        // Act
        Map<String, Long> result = rideService.getCarTypeRideCountForRider(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rideRepository, times(1)).findAllByUserId(testUserId);
    }
}