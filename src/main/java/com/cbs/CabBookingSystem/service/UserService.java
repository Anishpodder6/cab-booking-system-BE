package com.cbs.CabBookingSystem.service;

import com.cbs.CabBookingSystem.dto.UserRegistrationDto;
import com.cbs.CabBookingSystem.model.User;
import com.cbs.CabBookingSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class UserService {

    @Autowired
    private UserRepository userRepository;

//    public User registerUser(UserRegistrationDto registrationDto) {
//        User user = new User();
////        user.setFirstName(registrationDto.getName());
//    }

}
