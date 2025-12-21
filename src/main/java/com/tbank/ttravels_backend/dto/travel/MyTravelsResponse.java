package com.tbank.ttravels_backend.dto.travel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Ответ с списком моих поездок")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyTravelsResponse {
    @Schema(description = "Список поездок пользователя")
    private List<MyTravelItem> travels;
}

