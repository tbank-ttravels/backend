package com.tbank.ttravels_backend.dto.category;

import lombok.*;

@Getter
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private Long travelId;
    private String name;
}
