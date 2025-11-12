package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RiderAllDetailsResponseDTO;
import com.cbs.CabBookingSystem.dto.RiderRegistrationResponseDTO;
import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.dto.UserUpdateDto;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.UserPrincipal;
import com.cbs.CabBookingSystem.repository.PaymentRepository;
import com.cbs.CabBookingSystem.repository.RatingRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RatingRepository ratingRepository;


    private RiderRegistrationResponseDTO mapUserToResponseDTO(User user) {
        RiderRegistrationResponseDTO dto = new RiderRegistrationResponseDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRole("RIDER");
        log.debug("Mapped User entity to RiderRegistrationResponseDTO for user ID: {}", user.getUserId());
        return dto;
    }

    public User findUserByEmail(String email){
        log.info("Attempting to find user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new ResourceNotFoundException("User not found with email: " + email);
                });
    }

    public RiderRegistrationResponseDTO findUserById(UUID userId) {
        log.info("Attempting to find user by ID: {}", userId);
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID : " + userId);
                });
        log.info("Found user with ID: {}", userId);
        return mapUserToResponseDTO(userEntity);
    }

    public RiderRegistrationResponseDTO updateUserProfileById(UserUpdateDto userUpdateDto, UUID userId) {
        log.info("Attempting to update profile for user ID: {}", userId);
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot update profile. User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User not found with ID : " + userId);
                });

        log.debug("Existing user data for ID {}: {}", userId, existingUser);

        existingUser.setFirstName(userUpdateDto.getFirstName());
        existingUser.setLastName(userUpdateDto.getLastName());
        existingUser.setPhone(userUpdateDto.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(existingUser);
        log.info("Successfully updated profile for user ID: {}", savedUser.getUserId());
        log.debug("Updated User Profile: {}", savedUser);
        return mapUserToResponseDTO(savedUser);
    }


    public void deleteUserById(UUID userId) {
        log.warn("Attempting to delete user with ID: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);
        if (!userOptional.isPresent()) {
            log.error("Cannot delete user. User not found with ID: {}", userId);
            throw new ResourceNotFoundException("User not found with ID : " + userId);
        }

        User userToDelete = userOptional.get();
        log.info("User details before deletion: {}", userToDelete);

        userRepository.deleteById(userId);
        log.info("Successfully deleted user with ID: {}", userId);
    }


    public RiderAllDetailsResponseDTO getRiderAllDetails(UUID userId) {
        log.info("Attempting to fetch all details for rider ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Cannot retrieve rider details. User not found with ID: {}", userId);
                    return new UsernameNotFoundException("User not found with ID: " + userId);
                });


        RiderAllDetailsResponseDTO riderAllDetailsResponseDTO = new RiderAllDetailsResponseDTO(user);

        aggregateMetrics(user, riderAllDetailsResponseDTO);
        log.info("Successfully aggregated metrics for rider ID: {}", userId);
        return riderAllDetailsResponseDTO;

    }

    public void aggregateMetrics(User rider, RiderAllDetailsResponseDTO detailsDTO) {
        UUID riderId = rider.getUserId();
        log.debug("Starting metric aggregation for rider ID: {}", riderId);

        detailsDTO.setUserId(rider.getUserId());
        detailsDTO.setFirstName(rider.getFirstName());
        detailsDTO.setLastName(rider.getLastName());
        detailsDTO.setEmail(rider.getEmail());
        detailsDTO.setPhone(rider.getPhone());
        detailsDTO.setCreatedAt(rider.getCreatedAt());
        detailsDTO.setUpdatedAt(rider.getUpdatedAt());

        LocalDateTime startOfTodayLocal = LocalDateTime.now().toLocalDate().atStartOfDay();
        log.debug("Calculating metrics since: {}", startOfTodayLocal);

        Long totalRidesValue = rideRepository.countByUserId(riderId);
        Long todayRidesValue = rideRepository.countRidesSince(riderId, startOfTodayLocal);

        detailsDTO.setTotalRides(totalRidesValue != null ? totalRidesValue.intValue() : 0);
        detailsDTO.setTodayRides(todayRidesValue != null ? todayRidesValue.intValue() : 0);
        log.debug("Total Rides: {}, Today's Rides: {}", detailsDTO.getTotalRides(), detailsDTO.getTodayRides());

        Double totalSpent = paymentRepository.sumTotalAmountByUserId(riderId);
        Double todaySpent = paymentRepository.sumAmountSince(riderId, startOfTodayLocal);

        detailsDTO.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
        detailsDTO.setTodaySpent(todaySpent != null ? todaySpent : 0.0);
        log.debug("Total Spent: {}, Today's Spent: {}", detailsDTO.getTotalSpent(), detailsDTO.getTodaySpent());

        Double avgRating = ratingRepository.calculateAverageRatingByUserId(riderId);

        detailsDTO.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        log.debug("Average Rating: {}", detailsDTO.getRating());

        log.debug("Metric aggregation complete for rider ID: {}", riderId);
    }
}