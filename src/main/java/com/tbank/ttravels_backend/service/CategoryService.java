package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.dto.category.*;
import com.tbank.ttravels_backend.dto.category.validator.CategoryValidator;
import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.Travel;
import com.tbank.ttravels_backend.exception.CategoryNotFoundException;
import com.tbank.ttravels_backend.exception.TravelNotFoundException;
import com.tbank.ttravels_backend.repository.CategoryRepository;
import com.tbank.ttravels_backend.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TravelRepository travelRepository;

    @Transactional
    public CategoryResponse create(CreateCategoryRequest req) {

        CategoryValidator.validateCreate(req);

        Travel travel = travelRepository.findById(req.getTravelId())
                .orElseThrow(() -> new TravelNotFoundException("Поездка не найдена"));

        Category category = Category.builder()
                .travel(travel)
                .name(req.getName())
                .build();

        Category saved = categoryRepository.save(category);

        return new CategoryResponse(saved.getId(), saved.getTravel().getId(), saved.getName());
    }

    public Long getTravelIdByCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .map(c -> c.getTravel().getId())
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));
    }

    @Transactional
    public CategoryResponse edit(Long id, EditCategoryRequest req) {

        CategoryValidator.validateEdit(req.getName());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Категория не найдена"));

        category.setName(req.getName());

        Category saved = categoryRepository.save(category);

        return new CategoryResponse(saved.getId(), saved.getTravel().getId(), saved.getName());
    }

    public CategoriesListResponse getByTravel(Long travelId) {

        List<CategoryResponse> items = categoryRepository.findAllByTravel_Id(travelId)
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getTravel().getId(), c.getName()))
                .toList();

        return new CategoriesListResponse(items);
    }

    public List<Category> findAllCategoryInTravel(Long travelId) {
        return this.categoryRepository.findAllByTravel_Id(travelId);
    }
    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Категория с id = " + categoryId + " не найдена"));
    }
}
