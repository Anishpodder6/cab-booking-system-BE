package com.cbs.vector.repository;

import com.cbs.vector.model.CarData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarDataRepository extends JpaRepository<CarData, String> {
}