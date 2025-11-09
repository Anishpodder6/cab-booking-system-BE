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
        dto.setUserId(user.getUserId()); // Assuming your DTO has a matching field
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRole("RIDER");

        return dto;
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public RiderRegistrationResponseDTO findUserById(UUID userId) {
        User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + userId));
         return mapUserToResponseDTO(userEntity);
    }

    public RiderRegistrationResponseDTO updateUserProfileById(UserUpdateDto userUpdateDto, UUID userId) {

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID : " + userId));
        existingUser.setFirstName(userUpdateDto.getFirstName());
        existingUser.setLastName(userUpdateDto.getLastName());
        existingUser.setPhone(userUpdateDto.getPhone());
        existingUser.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(existingUser);
        log.info("Updated User Profile: " + savedUser);
        return mapUserToResponseDTO(savedUser);
    }


    public void deleteUserById(UUID userId) {
        if (!userRepository.findById(userId).isPresent()) {
            log.error("User not found with ID : " + userId);
            throw new ResourceNotFoundException("User not found with ID : " + userId);
        }
        log.info("Deleted User Profile: " + userRepository.findById(userId).get());
        userRepository.deleteById(userId);
    }


    public RiderAllDetailsResponseDTO getRiderAllDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));


        RiderAllDetailsResponseDTO riderAllDetailsResponseDTO = new RiderAllDetailsResponseDTO(user);

        aggregateMetrics(user, riderAllDetailsResponseDTO);
        return riderAllDetailsResponseDTO;

    }

    public void aggregateMetrics(User rider, RiderAllDetailsResponseDTO detailsDTO) {
        UUID riderId = rider.getUserId();

        detailsDTO.setUserId(rider.getUserId());
        detailsDTO.setFirstName(rider.getFirstName());
        detailsDTO.setLastName(rider.getLastName());
        detailsDTO.setEmail(rider.getEmail());
        detailsDTO.setPhone(rider.getPhone());
        detailsDTO.setCreatedAt(rider.getCreatedAt());
        detailsDTO.setUpdatedAt(rider.getUpdatedAt());

        LocalDateTime startOfTodayLocal = LocalDateTime.now().toLocalDate().atStartOfDay();

        Long totalRidesValue = rideRepository.countByUserId(riderId);
        Long todayRidesValue = rideRepository.countRidesSince(riderId, startOfTodayLocal); // <-- FIXED

        detailsDTO.setTotalRides(totalRidesValue != null ? totalRidesValue.intValue() : 0);
        detailsDTO.setTodayRides(todayRidesValue != null ? todayRidesValue.intValue() : 0);

        Double totalSpent = paymentRepository.sumTotalAmountByUserId(riderId);
        Double todaySpent = paymentRepository.sumAmountSince(riderId, startOfTodayLocal); // <-- FIXED to use LocalDateTime

        detailsDTO.setTotalSpent(totalSpent != null ? totalSpent : 0.0);
        detailsDTO.setTodaySpent(todaySpent != null ? todaySpent : 0.0);
        Double avgRating = ratingRepository.calculateAverageRatingByUserId(riderId);

        detailsDTO.setRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
    }
}
