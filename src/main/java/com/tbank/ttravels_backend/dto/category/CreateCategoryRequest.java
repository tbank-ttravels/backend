package com.tbank.ttravels_backend.dto.category;

import lombok.*;

@Getter @Setter
public class CreateCategoryRequest {
    private Long travelId;
    private String name;
}
