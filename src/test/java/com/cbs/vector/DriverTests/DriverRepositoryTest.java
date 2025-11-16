package com.cbs.vector.DriverTests;

import com.cbs.vector.model.Driver;
import com.cbs.vector.model.DriverStatus;
import com.cbs.vector.model.PersonalDetails;
import com.cbs.vector.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DriverRepositoryTest {

    @Mock
    private DriverRepository driverRepository;

    @Test
    void testFindByPersonalDetailsEmail() {
        String email = "test@example.com";
        Driver driver = new Driver();
        PersonalDetails personalDetails = new PersonalDetails();
        personalDetails.setEmail(email);
        driver.setPersonalDetails(personalDetails);

        when(driverRepository.findByPersonalDetailsEmail(email)).thenReturn(Optional.of(driver));

        Optional<Driver> result = driverRepository.findByPersonalDetailsEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getPersonalDetails().getEmail());
        verify(driverRepository, times(1)).findByPersonalDetailsEmail(email);
    }


    @Test
    void testFindByStatus() {
        DriverStatus status = DriverStatus.AVAILABLE;
        Driver driver1 = new Driver();
        driver1.setStatus(status);
        Driver driver2 = new Driver();
        driver2.setStatus(status);

        List<Driver> drivers = List.of(driver1, driver2);

        when(driverRepository.findByStatus(status)).thenReturn(drivers);

        List<Driver> result = driverRepository.findByStatus(status);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(d -> d.getStatus() == status));
        verify(driverRepository, times(1)).findByStatus(status);
    }
}
