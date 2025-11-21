package com.tbank.ttravels_backend.service;

import com.tbank.ttravels_backend.entity.Category;
import com.tbank.ttravels_backend.entity.User;
import com.tbank.ttravels_backend.exception.CategoryNotFoundException;
import com.tbank.ttravels_backend.repository.CategoryRepository;
import com.tbank.ttravels_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class ReferenceLookupService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;


    public ReferenceLookupService(CategoryRepository categoryRepository,
                                  UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }


    public Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new CategoryNotFoundException("Категория с id = " + categoryId + " не найдена"));
    }


    public List<User> getUsers(Set<Long> userIds) {
        return this.userRepository.findAllById(userIds);
    }


    public List<Category> findAllCategoryInTravel(Long travelId) {
        return this.categoryRepository.findAllByTravel_Id(travelId);
    }
}
