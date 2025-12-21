package com.tbank.ttravels_backend.controller;

import com.tbank.ttravels_backend.dto.category.CategoriesListResponse;
import com.tbank.ttravels_backend.dto.category.CategoryResponse;
import com.tbank.ttravels_backend.dto.category.CreateCategoryRequest;
import com.tbank.ttravels_backend.dto.category.EditCategoryRequest;
import com.tbank.ttravels_backend.security.UserPrincipal;
import com.tbank.ttravels_backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/travels/{travelId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public CategoryResponse create(
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateCategoryRequest request
    ) {
        return categoryService.create(travelId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id) && @travelSecurity.isTravelOpen(#travelId)")
    public CategoryResponse edit(
            @PathVariable Long travelId,
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody EditCategoryRequest request
    ) {
        return categoryService.edit(travelId, id, request);
    }

    @GetMapping()
    @PreAuthorize("@travelSecurity.isMember(#travelId, principal.id)")
    public CategoriesListResponse list(
            @PathVariable Long travelId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return categoryService.getByTravel(travelId);
    }
}

