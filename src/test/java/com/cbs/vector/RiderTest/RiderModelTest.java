package com.cbs.vector.RiderTest;

import com.cbs.vector.model.User;
import com.cbs.vector.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RiderModelTest {

    @Test
    void testUserFieldsAndUserDetailsMethods() {
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPhone("1234567890");
        user.setPasswordHash("hashedPassword");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setRole(UserRole.RIDER);
        user.setTodaySpent(10.5);
        user.setTodayRides(2);
        user.setRating(4.8);
        user.setTotalSpent(100.0);
        user.setTotalRides(20);

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("1234567890", user.getPhone());
        assertEquals("hashedPassword", user.getPassword());
        assertEquals(UserRole.RIDER, user.getRole());
        assertEquals(10.5, user.getTodaySpent());
        assertEquals(2, user.getTodayRides());
        assertEquals(4.8, user.getRating());
        assertEquals(100.0, user.getTotalSpent());
        assertEquals(20, user.getTotalRides());

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_RIDER", authorities.iterator().next().getAuthority());

        assertEquals("john.doe@example.com", user.getUsername());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }
}
