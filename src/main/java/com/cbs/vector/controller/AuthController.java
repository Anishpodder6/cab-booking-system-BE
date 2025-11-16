package com.cbs.vector.controller;

import com.cbs.vector.dto.*;
import com.cbs.vector.service.AuthService;
import com.cbs.vector.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200/")
@Slf4j
public class AuthController {


    private final AuthService authService;

    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/auth/register/rider
    @PostMapping("/register/rider")
    public ResponseEntity<RiderRegistrationResponseDTO> registerRider(@Valid @RequestBody UserRegistrationDto registrationDTO) {
        RiderRegistrationResponseDTO newRider = authService.registerRider(registrationDTO);
//        newRider.set(null); // Remove hash from response
        log.info("Successfully registered new rider. E-mail: {}", newRider.getEmail());
        return new ResponseEntity<>(newRider, HttpStatus.CREATED);
    }

    // POST /api/auth/register/driver
    @PostMapping("/register/driver")
    public ResponseEntity<DriverResponseDTO> registerDriver(@RequestBody DriverRegistrationDTO registrationDTO) {
        DriverResponseDTO responseDTO = authService.registerDriver(registrationDTO);
        log.info("Successfully registered new driver. ID: {}", responseDTO.getId());
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginDTO) {
        try {
            UserDetails userDetails = authService.loginUser(loginDTO);

            String token = jwtUtil.generateToken(userDetails);

            AuthResponseDTO responseDTO = new AuthResponseDTO();
            responseDTO.setToken(token);
            responseDTO.setEmail(loginDTO.email());

            String role = userDetails.getAuthorities().iterator().next().getAuthority().substring(5); // Removes "ROLE_"
            responseDTO.setRole(role);
            String userId = jwtUtil.extractUserId(token);
            responseDTO.setUserId(userId);
            log.info("User {} successfully logged in with role {}. User ID: {}", loginDTO.email(), role, userId);
            return ResponseEntity.ok(responseDTO);

        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            log.warn("Login failed: Invalid credentials provided :{}", ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
