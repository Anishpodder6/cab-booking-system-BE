
package com.cbs.CabBookingSystem.RiderTest;

import com.cbs.CabBookingSystem.dto.RiderAllDetailsResponseDTO;
import com.cbs.CabBookingSystem.dto.RiderRegistrationResponseDTO;
import com.cbs.CabBookingSystem.dto.UserUpdateDto;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.enums.UserRole;
import com.cbs.CabBookingSystem.repository.PaymentRepository;
import com.cbs.CabBookingSystem.repository.RatingRepository;
import com.cbs.CabBookingSystem.repository.RideRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import com.cbs.CabBookingSystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RiderControllerTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RideRepository rideRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RatingRepository ratingRepository;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setUserId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPhone("1234567890");
        testUser.setRole(UserRole.RIDER);
        testUser.setCreatedAt(LocalDateTime.now().minusDays(1));
        testUser.setUpdatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    void findUserById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        RiderRegistrationResponseDTO result = userService.findUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("John", result.getFirstName());
    }

    @Test
    void findUserById_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(userId));
    }

    @Test
    void updateUserProfileById_success() {
        UserUpdateDto update = new UserUpdateDto();
        update.setFirstName("Jane");
        update.setLastName("Smith");
        update.setPhone("555-0000");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RiderRegistrationResponseDTO updated = userService.updateUserProfileById(update, userId);

        assertNotNull(updated);
        assertEquals("Jane", updated.getFirstName());
        assertEquals("Smith", updated.getLastName());
    }

    @Test
    void updateUserProfileById_notFound_throws() {
        UserUpdateDto update = new UserUpdateDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserProfileById(update, userId));
    }

    @Test
    void deleteUserById_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(userId);

        assertDoesNotThrow(() -> userService.deleteUserById(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void deleteUserById_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(userId));
    }

    @Test
    void getRiderAllDetails_success_and_metrics_aggregated() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(rideRepository.countByUserId(userId)).thenReturn(5L);
        when(rideRepository.countRidesSince(eq(userId), any(LocalDateTime.class))).thenReturn(2L);
        when(paymentRepository.sumTotalAmountByUserId(userId)).thenReturn(123.45);
        when(paymentRepository.sumAmountSince(eq(userId), any(LocalDateTime.class))).thenReturn(10.0);

        // Direct stubbing of the repository method (fixes the "Cannot resolve method 'thenReturn(double)'" error)
        when(ratingRepository.calculateAverageRatingByUserId(userId)).thenReturn(4.25);

        RiderAllDetailsResponseDTO details = userService.getRiderAllDetails(userId);

        assertNotNull(details);
        assertEquals(userId, details.getUserId());
        assertEquals(5, details.getTotalRides());
        assertEquals(2, details.getTodayRides());
        assertEquals(123.45, details.getTotalSpent());
        assertEquals(10.0, details.getTodaySpent());
        assertEquals(4.3, details.getRating());
    }

    @Test
    void getRiderAllDetails_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getRiderAllDetails(userId));
    }
}
