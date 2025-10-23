package com.cbs.CabBookingSystem.repository;

import com.cbs.CabBookingSystem.model.RideHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RideHistoryRepository extends JpaRepository<RideHistory, Long> {

}