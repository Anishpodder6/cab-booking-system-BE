package com.cbs.vector.RiderTest;

import com.cbs.vector.dto.*;
import com.cbs.vector.exception.ResourceNotFoundException;
import com.cbs.vector.model.User;
import com.cbs.vector.model.enums.UserRole;
import com.cbs.vector.repository.*;
import com.cbs.vector.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RiderServiceTest {

    @InjectMocks
    private UserService riderService;

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
    }

    @Test
    void testFindUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        RiderRegistrationResponseDTO result = riderService.findUserById(userId);
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
    }

    @Test
    void testFindUserById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> riderService.findUserById(userId));
    }

    @Test
    void testUpdateUserProfileById_Success() {
        UserUpdateDto updateDto = new UserUpdateDto();
        updateDto.setFirstName("Jane");
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RiderRegistrationResponseDTO result = riderService.updateUserProfileById(updateDto, userId);
        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
    }

    @Test
    void testUpdateUserProfileById_NotFound() {
        UserUpdateDto updateDto = new UserUpdateDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> riderService.updateUserProfileById(updateDto, userId));
    }

    @Test
    void testDeleteUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(userId);
        assertDoesNotThrow(() -> riderService.deleteUserById(userId));
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testDeleteUserById_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            // You need to update UserService.deleteUserById to check existence and throw
            riderService.deleteUserById(userId);
        });
    }

    @Test
    void testGetRiderAllDetails_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(rideRepository.countByUserId(userId)).thenReturn(0L);
        when(rideRepository.countRidesSince(eq(userId), any())).thenReturn(0L);
        when(paymentRepository.sumTotalAmountByUserId(userId)).thenReturn(0.0);
        when(paymentRepository.sumAmountSince(eq(userId), any())).thenReturn(0.0);
        when(ratingRepository.calculateAverageRatingByUserId(userId)).thenReturn(0.0);

        RiderAllDetailsResponseDTO result = riderService.getRiderAllDetails(userId);
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
    }

    @Test
    void testGetRiderAllDetails_NotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> riderService.getRiderAllDetails(userId));
    }
}
