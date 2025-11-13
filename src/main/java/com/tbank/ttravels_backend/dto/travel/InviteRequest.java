package com.tbank.ttravels_backend.dto.travel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteRequest {
    @NotBlank(message = "Поле обязательно!")
    private String phone;
}
