package com.cbs.CabBookingSystem.controller;

import com.cbs.CabBookingSystem.dto.*;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.UserPrincipal;
import com.cbs.CabBookingSystem.service.AuthService;
import com.cbs.CabBookingSystem.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200/")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    // POST /api/auth/register/rider
    @PostMapping("/register/rider")
    public ResponseEntity<RiderRegistrationResponseDTO> registerRider(@Valid @RequestBody UserRegistrationDto registrationDTO) {
        RiderRegistrationResponseDTO newRider = authService.registerRider(registrationDTO);
//        newRider.set(null); // Remove hash from response
        return new ResponseEntity<>(newRider, HttpStatus.CREATED);
    }

    @PostMapping("/register/driver")
    public ResponseEntity<DriverResponseDTO> registerDriver(@RequestBody DriverRegistrationDTO registrationDTO) {
        DriverResponseDTO responseDTO = authService.registerDriver(registrationDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO loginDTO) {
        try {
            // 1. Authenticate and retrieve UserDetails based on email, password, and role
            UserDetails userDetails = authService.loginUser(loginDTO);

//            UUID userId = null;
//            if(userDetails instanceof UserPrincipal){
//                userId = ((UserPrincipal) userDetails).getUserId();
//            }
            // 2. Generate and return JWT token
            String token = jwtUtil.generateToken(userDetails);

            AuthResponseDTO responseDTO = new AuthResponseDTO();
            responseDTO.setToken(token);
            responseDTO.setEmail(loginDTO.email());

            // Extract role from the granted authority
            String role = userDetails.getAuthorities().iterator().next().getAuthority().substring(5); // Removes "ROLE_"
            responseDTO.setRole(role);
            String userId = jwtUtil.extractUserId(token);
            responseDTO.setUserId(userId);
            return ResponseEntity.ok(responseDTO);

        } catch (UsernameNotFoundException | IllegalArgumentException e) {
            // Catches user not found or invalid credentials (invalid password)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
