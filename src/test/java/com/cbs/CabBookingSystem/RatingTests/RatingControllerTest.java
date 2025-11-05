package com.cbs.CabBookingSystem.RatingTests;

import com.cbs.CabBookingSystem.controller.RatingAndFeedbackController;
import com.cbs.CabBookingSystem.dto.RatingDTO;
import com.cbs.CabBookingSystem.service.RatingService;
import com.cbs.CabBookingSystem.service.UserDetailsServiceImpl;
import com.cbs.CabBookingSystem.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RatingAndFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RatingService ratingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService; // Mock the new missing dependency

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private UUID driverId;
    private UUID rideId;
    private RatingDTO ratingDTO;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        driverId = UUID.randomUUID();
        rideId = UUID.randomUUID();

        ratingDTO = new RatingDTO();
        ratingDTO.setUserId(userId);
        ratingDTO.setDriverId(driverId);
        ratingDTO.setRideId(rideId);
        ratingDTO.setRating(5);
        ratingDTO.setComments("Excellent service");
    }

    @Test
    void createRating_ShouldReturnCreated() throws Exception {
        when(ratingService.createRating(any(RatingDTO.class))).thenReturn(ratingDTO);

        mockMvc.perform(post("/api/ratings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void getRatingsByUserId_WhenRatingsExist_ShouldReturnOk() throws Exception {
        List<RatingDTO> ratings = List.of(ratingDTO);
        when(ratingService.getRatingsByUserId(userId)).thenReturn(ratings);

        mockMvc.perform(get("/api/ratings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRatingsByUserId_WhenNoRatings_ShouldReturnNoContent() throws Exception {
        when(ratingService.getRatingsByUserId(userId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/ratings/user/{userId}", userId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAverageRating_ShouldReturnOkWithValue() throws Exception {
        Double average = 4.5;
        when(ratingService.getUserAverageRating(userId)).thenReturn(average);

        mockMvc.perform(get("/api/ratings/user/{userId}/avgRating", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(average.toString()));
    }
}
