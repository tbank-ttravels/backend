package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.category.*;
import com.tbank.ttravels_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("@travelSecurity.isMember(#request.travelId, #userId)")
    public CategoryResponse create(
            @AuthenticationPrincipal(expression = "getId()") Long userId,
            @RequestBody CreateCategoryRequest request
    ) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@travelSecurity.isMember(@categoryService.getTravelIdByCategory(#id), #userId)")
    public CategoryResponse edit(
            @PathVariable Long id,
            @AuthenticationPrincipal(expression = "getId()") Long userId,
            @RequestBody EditCategoryRequest request
    ) {
        return categoryService.edit(id, request);
    }

    @GetMapping("/travel/{travelId}")
    @PreAuthorize("@travelSecurity.isMember(#travelId, #userId)")
    public CategoriesListResponse list(
            @PathVariable Long travelId,
            @AuthenticationPrincipal(expression = "getId()") Long userId
    ) {
        return categoryService.getByTravel(travelId);
    }
}

