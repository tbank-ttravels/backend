package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.exception.CategoryNotFoundException;
import com.tbank.ttravels_backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAllCategoryInTravel(Long travelId) {
        return this.categoryRepository.findAllByTravel_Id(travelId);
    }
    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Категория с id = " + categoryId + " не найдена"));
    }
}
