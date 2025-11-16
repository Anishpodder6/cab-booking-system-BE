package com.cbs.vector.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper newModelMapper = new ModelMapper();
        newModelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        return newModelMapper;
    }

}
