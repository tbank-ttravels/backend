package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByTravel_Id(Long travelId);
}
