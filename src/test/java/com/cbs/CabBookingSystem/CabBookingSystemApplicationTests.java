// `src/test/java/com/cbs/CabBookingSystem/CabBookingSystemApplicationTests.java`
package com.cbs.CabBookingSystem;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

@SpringBootTest
class CabBookingSystemApplicationTests {

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
}
