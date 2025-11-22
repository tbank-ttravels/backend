package com.tbank.ttravels_backend.dto.category;

import lombok.*;
import java.util.List;

@Getter
@AllArgsConstructor
public class CategoriesListResponse {
    private List<CategoryResponse> items;
}

