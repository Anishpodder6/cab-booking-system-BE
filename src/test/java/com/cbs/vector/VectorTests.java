package com.cbs.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class VectorApplicationTests {

    @Configuration
    static class TestDataSourceConfig {
        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }
    }

    @Test
    void contextLoads() {
        // Passes if the Spring context starts
    }

    @Test
    @DisplayName("VectorApplication class should be annotated with @SpringBootApplication")
    void vectorApplication_shouldHaveSpringBootApplicationAnnotation() {
        assertTrue(VectorApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
    }

}