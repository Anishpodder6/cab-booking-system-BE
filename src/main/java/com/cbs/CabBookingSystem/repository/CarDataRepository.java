package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.CarData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarDataRepository extends JpaRepository<CarData, String> {
}