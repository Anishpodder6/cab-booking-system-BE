package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.RiderRegistrationResponseDTO;
import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.dto.UserUpdateDto;
import com.cbs.CabBookingSystem.exception.ResourceNotFoundException;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.UserPrincipal;
import com.cbs.CabBookingSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

//    public User registerUser(UserRegistrationDto registrationDto) {
//        User user = new User();
//        user.setFirstName(registrationDto.getFirstName());
//        user.setLastName(registrationDto.getLastName());
//        user.setEmail(registrationDto.getEmail());
//        user.setPhone(registrationDto.getPhone());
//        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPasswordHash()));
//        user.setCreatedAt(LocalDateTime.now());
//        return userRepository.save(user);
//    }

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

        User savedUser = userRepository.save(existingUser);
        return mapUserToResponseDTO(savedUser);
    }


    public void deleteUserById(UUID userId) {
        userRepository.deleteById(userId);
    }

}
