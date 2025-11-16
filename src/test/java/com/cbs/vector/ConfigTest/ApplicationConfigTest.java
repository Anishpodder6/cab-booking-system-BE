package com.cbs.vector.ConfigTest;

import com.cbs.vector.config.ApplicationConfig;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationConfigTest {

    private final ApplicationConfig applicationConfig = new ApplicationConfig();

    @Test
    void testModelMapperBeanCreation() {
        ModelMapper modelMapper = applicationConfig.modelMapper();
        assertNotNull(modelMapper, "ModelMapper bean should not be null");
    }

    @Test
    void testModelMapperStrictMatchingStrategy() {
        ModelMapper modelMapper = applicationConfig.modelMapper();
        assertEquals(MatchingStrategies.STRICT,
                modelMapper.getConfiguration().getMatchingStrategy(),
                "ModelMapper should use STRICT matching strategy");
    }

    @Test
    void testModelMapperBeanIsNotSingleton() {
        ModelMapper modelMapper1 = applicationConfig.modelMapper();
        ModelMapper modelMapper2 = applicationConfig.modelMapper();
        assertNotSame(modelMapper1, modelMapper2,
                "Each call should return a new ModelMapper instance");
    }
}
