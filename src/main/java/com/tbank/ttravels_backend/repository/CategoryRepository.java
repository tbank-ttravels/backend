package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByTravel_Id(Long travelId);

    Optional<Category> findByIdAndTravel_Id(Long id, Long travelId);
}
