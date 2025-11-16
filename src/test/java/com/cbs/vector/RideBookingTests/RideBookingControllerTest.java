package com.cbs.vector.RideBookingTests;

import com.cbs.vector.controller.RideBookingController;
import com.cbs.vector.dto.RideDto;
import com.cbs.vector.model.CarData;
import com.cbs.vector.model.Ride;
import com.cbs.vector.model.enums.RideStatus;
import com.cbs.vector.repository.CarDataRepository;
import com.cbs.vector.service.RideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RideBookingControllerTest {

    @Mock
    private RideService rideService;

    @Mock
    private CarDataRepository carDataRepository;

    @InjectMocks
    private RideBookingController rideBookingController;

    private Ride testRide;
    private RideDto testRideDto;
    private UUID testRideId;
    private UUID testUserId;
    private CarData testCarData;

    @BeforeEach
    void setUp() {
        testRideId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testRide = new Ride();
        testRide.setRideId(testRideId);
        testRide.setUserId(testUserId);
        testRide.setPickupLocation("Test Pickup");
        testRide.setDropLocation("Test Drop");
        testRide.setFare(100.0);
        testRide.setStatus(RideStatus.LookingForDriver);
        testRide.setCarType("Sedan");

        testRideDto = new RideDto();
        testRideDto.setUserId(testUserId);
        testRideDto.setPickupLocation("Test Pickup");
        testRideDto.setDropLocation("Test Drop");
        testRideDto.setFare(100.0);
        testRideDto.setStatus(RideStatus.LookingForDriver);
        testRideDto.setCarType("Sedan");

        testCarData = new CarData("1", "Sedan", new BigDecimal("12.50"));
    }

    @Test
    void getRideDetails_ShouldReturnRide_WhenValidId() {
        // Arrange
        when(rideService.getRideById(testRideId)).thenReturn(testRide);

        // Act
        ResponseEntity<Ride> response = rideBookingController.getRideDetails(testRideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRideId, response.getBody().getRideId());
        // Verify only once despite the log statement
        verify(rideService, times(1)).getRideById(testRideId);
    }

    @Test
    void bookRide_ShouldCreateRide_WhenValidRideDto() {
        // Arrange
        when(rideService.addRide(any(RideDto.class))).thenReturn(testRide);

        // Act
        ResponseEntity<Ride> response = rideBookingController.bookRide(testRideDto);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRideId, response.getBody().getRideId());
        verify(rideService, times(1)).addRide(testRideDto);
    }

    @Test
    void patchRideData_ShouldUpdateRide_WhenValidInput() {
        // Arrange
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "ConfirmedByDriver");

        when(rideService.patchRideData(updates, testRideId)).thenReturn(testRide);

        // Act
        ResponseEntity<Ride> response = rideBookingController.patchRideData(testRideId, updates);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRideId, response.getBody().getRideId());
        verify(rideService, times(1)).patchRideData(updates, testRideId);
    }

    @Test
    void getUnassignedRides_ShouldReturnRideList() {
        // Arrange
        List<Ride> unassignedRides = Arrays.asList(testRide);
        when(rideService.getUnassignedRides()).thenReturn(unassignedRides);

        // Act
        ResponseEntity<List<Ride>> response = rideBookingController.getUnassignedRides();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        // Use atLeastOnce() instead of times(1) to handle the log statement call
        verify(rideService, atLeastOnce()).getUnassignedRides();
    }

    @Test
    void getUnassignedRides_ShouldReturnEmptyList_WhenNoRides() {
        // Arrange
        List<Ride> emptyList = Collections.emptyList();
        when(rideService.getUnassignedRides()).thenReturn(emptyList);

        // Act
        ResponseEntity<List<Ride>> response = rideBookingController.getUnassignedRides();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(rideService, atLeastOnce()).getUnassignedRides();
    }

    @Test
    void getRiderUpcomingRides_ShouldReturnRideList() {
        // Arrange
        List<Ride> upcomingRides = Arrays.asList(testRide);
        when(rideService.getRiderUpcomingRide(testUserId)).thenReturn(upcomingRides);

        // Act
        ResponseEntity<List<Ride>> response = rideBookingController.getRiderUpcomingRides(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(rideService, atLeastOnce()).getRiderUpcomingRide(testUserId);
    }

    @Test
    void getDriverUpcomingRides_ShouldReturnRideList() {
        // Arrange
        List<Ride> upcomingRides = Arrays.asList(testRide);
        when(rideService.getDriverUpcomingRide(testUserId)).thenReturn(upcomingRides);

        // Act
        ResponseEntity<List<Ride>> response = rideBookingController.getDriverUpcomingRides(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(rideService, atLeastOnce()).getDriverUpcomingRide(testUserId);
    }

    @Test
    void hasTwoRides_ShouldReturnTrue_WhenUserHasTwoOrMoreRides() {
        // Arrange
        when(rideService.hasTwoRides(testUserId)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = rideBookingController.hasTwoRides(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody());
        verify(rideService, atLeastOnce()).hasTwoRides(testUserId);
    }

    @Test
    void hasTwoRides_ShouldReturnFalse_WhenUserHasLessThanTwoRides() {
        // Arrange
        when(rideService.hasTwoRides(testUserId)).thenReturn(false);

        // Act
        ResponseEntity<Boolean> response = rideBookingController.hasTwoRides(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody());
        verify(rideService, atLeastOnce()).hasTwoRides(testUserId);
    }

    @Test
    void getAllCarFares_ShouldReturnCarDataList() {
        // Arrange
        List<CarData> carDataList = Arrays.asList(testCarData);
        when(carDataRepository.findAll()).thenReturn(carDataList);

        // Act
        ResponseEntity<List<CarData>> response = rideBookingController.getAllCallFares();

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Sedan", response.getBody().get(0).getType());
        verify(carDataRepository, atLeastOnce()).findAll();
    }

    @Test
    void getRideStatus_ShouldReturnStatus() {
        // Arrange
        when(rideService.getRideStatus(testRideId)).thenReturn(RideStatus.ConfirmedByDriver);

        // Act
        ResponseEntity<RideStatus> response = rideBookingController.getRideStatus(testRideId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(RideStatus.ConfirmedByDriver, response.getBody());
        verify(rideService, atLeastOnce()).getRideStatus(testRideId);
    }

    @Test
    void getRiderCarTypeCount_ShouldReturnCarTypeCounts() {
        // Arrange
        Map<String, Long> carTypeCounts = new HashMap<>();
        carTypeCounts.put("Sedan", 5L);
        carTypeCounts.put("SUV", 3L);

        when(rideService.getCarTypeRideCountForRider(testUserId)).thenReturn(carTypeCounts);

        // Act
        ResponseEntity<Map<String, Long>> response = rideBookingController.getRiderCarTypeCount(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(5L, response.getBody().get("Sedan"));
        assertEquals(3L, response.getBody().get("SUV"));
        verify(rideService, times(1)).getCarTypeRideCountForRider(testUserId);
    }

    @Test
    void getRiderCarTypeCount_ShouldReturnNoContent_WhenEmptyResult() {
        // Arrange
        Map<String, Long> emptyMap = Collections.emptyMap();
        when(rideService.getCarTypeRideCountForRider(testUserId)).thenReturn(emptyMap);

        // Act
        ResponseEntity<Map<String, Long>> response = rideBookingController.getRiderCarTypeCount(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(rideService, times(1)).getCarTypeRideCountForRider(testUserId);
    }

    @Test
    void getRiderCarTypeCount_ShouldReturnNoContent_WhenNullResult() {
        // Arrange
        when(rideService.getCarTypeRideCountForRider(testUserId)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Long>> response = rideBookingController.getRiderCarTypeCount(testUserId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(rideService, times(1)).getCarTypeRideCountForRider(testUserId);
    }

    @Test
    void bookRide_ShouldHandleServiceExceptions() {
        // Arrange
        when(rideService.addRide(any(RideDto.class)))
        .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rideBookingController.bookRide(testRideDto);
        });

        verify(rideService, times(1)).addRide(testRideDto);
    }

    @Test
    void getRideDetails_ShouldHandleServiceExceptions() {
        // Arrange
        when(rideService.getRideById(testRideId))
        .thenThrow(new RuntimeException("Ride not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            rideBookingController.getRideDetails(testRideId);
        });

        verify(rideService, times(1)).getRideById(testRideId);
    }
}