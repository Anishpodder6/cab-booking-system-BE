package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.model.Driver;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.model.enums.UserRole;
import com.cbs.CabBookingSystem.repository.DriverRepository;
import com.cbs.CabBookingSystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;

    public UserDetailsServiceImpl(UserRepository userRepository, DriverRepository driverRepository) {
        this.userRepository = userRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElse(null);
        if(user != null) return user;

        Driver driver = driverRepository.findByPersonalDetailsEmail(email).orElse(null);
        if(driver != null) return driver;

        throw new UsernameNotFoundException("User not found with email : " + email);
    }

    public UserDetails loadUserByEmailAndRole(String email, UserRole role) throws UsernameNotFoundException{
        if(role == UserRole.RIDER){
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Rider not found with email : " + email));
        }

        if(role == UserRole.DRIVER){
            return driverRepository.findByPersonalDetailsEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Driver not found with email : " + email));
        }

        throw new UsernameNotFoundException("Invalid user role specified.");

    }

}
