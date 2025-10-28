package com.cbs.CabBookingSystem.config;

import com.cbs.CabBookingSystem.dto.RideDto;
import com.cbs.CabBookingSystem.model.Ride;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper newModelMapper = new ModelMapper();
        newModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return newModelMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(12);
    }
}
