package com.cbs.CabBookingSystem.DriverTests;

import com.cbs.CabBookingSystem.controller.DriverController;
import com.cbs.CabBookingSystem.dto.DriverResponseDTO;
import com.cbs.CabBookingSystem.dto.DriverUpdateDTO;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.DriverStatus;
import com.cbs.CabBookingSystem.model.RideWithRating;
import com.cbs.CabBookingSystem.service.DriverService;
import com.cbs.CabBookingSystem.service.RideService;
import com.cbs.CabBookingSystem.service.UserDetailsServiceImpl;
import com.cbs.CabBookingSystem.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false) // Disables security filters for focused controller testing
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DriverService driverService;

    @MockitoBean
    private RideService rideService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService; // Mocked to prevent security context issues

    @MockitoBean
    private JwtUtil jwtUtil; // Mocked to prevent security context issues

    @Test
    @DisplayName("GET /available - Should return list of available drivers")
    void getAvailableDrivers_shouldReturnListOfDrivers() throws Exception {
        DriverResponseDTO driverDTO = new DriverResponseDTO();
        driverDTO.setId(UUID.randomUUID());
        driverDTO.setName("Test Driver");
        driverDTO.setStatus(DriverStatus.AVAILABLE);

        when(driverService.getAvailableDrivers()).thenReturn(List.of(driverDTO));

        mockMvc.perform(get("/api/drivers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Driver"));

        verify(driverService).getAvailableDrivers();
    }

    @Test
    @DisplayName("PUT /status/{id} - Should return OK on successful status update")
    void updateDriverStatus_shouldReturnOkWhenSuccess() throws Exception {
        UUID driverId = UUID.randomUUID();
        DriverResponseDTO driverDTO = new DriverResponseDTO();
        driverDTO.setId(driverId);
        driverDTO.setStatus(DriverStatus.AVAILABLE);

        when(driverService.updateDriverStatus(driverId, DriverStatus.AVAILABLE)).thenReturn(Optional.of(driverDTO));

        mockMvc.perform(put("/api/drivers/status/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));

        verify(driverService).updateDriverStatus(driverId, DriverStatus.AVAILABLE);
    }

    @Test
    @DisplayName("PUT /status/{id} - Should return Not Found when driver does not exist")
    void updateDriverStatus_shouldReturnNotFoundWhenDriverMissing() throws Exception {
        UUID driverId = UUID.randomUUID();
        when(driverService.updateDriverStatus(driverId, DriverStatus.AVAILABLE)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/drivers/status/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"AVAILABLE\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /status/{id} - Should return Bad Request for an invalid status value")
    void updateDriverStatus_shouldReturnBadRequestForInvalidStatus() throws Exception {
        UUID driverId = UUID.randomUUID();
        mockMvc.perform(put("/api/drivers/status/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"INVALID_STATUS\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /profile/{id} - Should return OK on successful profile update")
    void updateDriver_shouldReturnOkWhenSuccess() throws Exception {
        UUID driverId = UUID.randomUUID();
        DriverUpdateDTO updateDTO = new DriverUpdateDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");

        DriverResponseDTO responseDTO = new DriverResponseDTO();
        responseDTO.setId(driverId);
        responseDTO.setName("Updated Name");

        when(driverService.updateDriverProfile(eq(driverId), any(DriverUpdateDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(put("/api/drivers/profile/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("PUT /profile/{id} - Should return Not Found when driver to update does not exist")
    void updateDriver_shouldReturnNotFoundWhenDriverMissing() throws Exception {
        UUID driverId = UUID.randomUUID();
        DriverUpdateDTO updateDTO = new DriverUpdateDTO();
        updateDTO.setFirstName("Updated");
        updateDTO.setLastName("Name");

        when(driverService.updateDriverProfile(eq(driverId), any(DriverUpdateDTO.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/drivers/profile/" + driverId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /profile/{id} - Should return driver details when found")
    void getDriverAllDetails_shouldReturnDriverWhenFound() throws Exception {
        UUID driverId = UUID.randomUUID();
        DriverResponseDTO driverDTO = new DriverResponseDTO();
        driverDTO.setId(driverId);
        driverDTO.setName("Test Driver");

        when(driverService.findUserById(driverId)).thenReturn(driverDTO);

        mockMvc.perform(get("/api/drivers/profile/" + driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(driverId.toString()));
    }

    @Test
    @DisplayName("GET /profile/{id} - Should return Not Found when driver does not exist")
    void getDriverAllDetails_shouldReturnNotFoundWhenMissing() throws Exception {
        UUID driverId = UUID.randomUUID();
        when(driverService.findUserById(driverId))
                .thenThrow(new ResourceNotFoundException("Driver not found"));

        mockMvc.perform(get("/api/drivers/profile/" + driverId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /history/{id} - Should return OK with ride history")
    void getDriverRideHistory_shouldReturnOkWithHistory() throws Exception {
        UUID driverId = UUID.randomUUID();
        when(rideService.getDriverHistory(driverId)).thenReturn(List.of(new RideWithRating()));

        mockMvc.perform(get("/api/drivers/history/" + driverId))
                .andExpect(status().isOk());

        verify(rideService).getDriverHistory(driverId);
    }

    @Test
    @DisplayName("GET /history/{id} - Should return OK with an empty list if no history exists")
    void getDriverRideHistory_shouldReturnOkWithEmptyList() throws Exception {
        UUID driverId = UUID.randomUUID();
        when(rideService.getDriverHistory(driverId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/drivers/history/" + driverId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}