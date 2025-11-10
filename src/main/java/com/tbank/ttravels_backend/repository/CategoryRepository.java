package com.tbank.ttravels_backend.repository;

import com.tbank.ttravels_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByTravel_Id(Long travelId);
}
