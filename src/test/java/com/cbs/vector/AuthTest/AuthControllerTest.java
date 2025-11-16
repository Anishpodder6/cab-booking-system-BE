package com.cbs.vector.AuthTest;

import com.cbs.vector.controller.AuthController;
import com.cbs.vector.dto.*;
import com.cbs.vector.service.AuthService;
import com.cbs.vector.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterRider_Success() {
        UserRegistrationDto registrationDto = mock(UserRegistrationDto.class);
        RiderRegistrationResponseDTO responseDTO = new RiderRegistrationResponseDTO();
        responseDTO.setEmail("test@example.com");

        when(authService.registerRider(registrationDto)).thenReturn(responseDTO);

        ResponseEntity<RiderRegistrationResponseDTO> response = authController.registerRider(registrationDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("test@example.com", response.getBody().getEmail());
    }

    @Test
    void testRegisterDriver_Success() {
        DriverRegistrationDTO registrationDto = mock(DriverRegistrationDTO.class);
        DriverResponseDTO responseDTO = new DriverResponseDTO();
        responseDTO.setId(java.util.UUID.randomUUID());

        when(authService.registerDriver(registrationDto)).thenReturn(responseDTO);

        ResponseEntity<DriverResponseDTO> response = authController.registerDriver(registrationDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().getId());
    }

    @Test
    void testLoginUser_Failure() {
        LoginRequestDTO loginDTO = new LoginRequestDTO("test@example.com", "wrong", com.cbs.vector.model.enums.UserRole.RIDER);

        when(authService.loginUser(loginDTO)).thenThrow(new UsernameNotFoundException("User not found"));

        ResponseEntity<AuthResponseDTO> response = authController.loginUser(loginDTO);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testLoginUser_Success() {
        // Arrange
        LoginRequestDTO loginDTO = new LoginRequestDTO(
                "test@example.com",
                "password123",
                com.cbs.vector.model.enums.UserRole.RIDER
        );

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        "test@example.com",
                        "hashedPassword",
                        java.util.Collections.singletonList(
                                new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_RIDER")
                        )
                );

        String mockToken = "mock.jwt.token";
        String mockUserId = "123e4567-e89b-12d3-a456-426614174000";

        when(authService.loginUser(loginDTO)).thenReturn(userDetails);
        when(jwtUtil.generateToken(userDetails)).thenReturn(mockToken);
        when(jwtUtil.extractUserId(mockToken)).thenReturn(mockUserId);

        // Act
        ResponseEntity<AuthResponseDTO> response = authController.loginUser(loginDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockToken, response.getBody().getToken());
        assertEquals("test@example.com", response.getBody().getEmail());
        assertEquals("RIDER", response.getBody().getRole());
        assertEquals(mockUserId, response.getBody().getUserId());

        verify(authService, times(1)).loginUser(loginDTO);
        verify(jwtUtil, times(1)).generateToken(userDetails);
        verify(jwtUtil, times(1)).extractUserId(mockToken);
    }

}
