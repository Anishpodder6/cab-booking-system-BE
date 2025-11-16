package com.cbs.vector.DriverTests;
import com.cbs.vector.model.*;
import com.cbs.vector.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DriverModelTest {

    @Test
    void testDriverCreationAndFields() {
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName("John");
        personalDetails.setLastName("Doe");
        personalDetails.setEmail("john.doe@example.com");
        personalDetails.setPhone("1234567890");
        personalDetails.setDateOfBirth(LocalDate.of(1990, 1, 1));
        personalDetails.setPassword("password123");
        personalDetails.setConfirmPassword("password123");

        Driver driver = new Driver();
        driver.setPersonalDetails(personalDetails);
        driver.onCreate();

        assertEquals("John Doe", driver.getName());
        assertEquals(UserRole.DRIVER, driver.getRole());
        assertNotNull(driver.getCreatedAt());
        assertNotNull(driver.getUpdatedAt());
        assertEquals("john.doe@example.com", driver.getUsername());
        assertEquals("password123", driver.getPassword());
        assertTrue(driver.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_DRIVER")));
    }

    @Test
    void testDriverUpdateName() {
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setFirstName("Jane");
        personalDetails.setLastName("Smith");
        personalDetails.setEmail("jane.smith@example.com");
        personalDetails.setPassword("pass");

        Driver driver = new Driver();
        driver.setPersonalDetails(personalDetails);
        driver.onCreate();

        personalDetails.setFirstName("Janet");
        driver.setPersonalDetails(personalDetails);
        driver.onUpdate();

        assertEquals("Janet Smith", driver.getName());
    }
}
