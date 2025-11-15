package com.tbank.ttravels_backend.dto.travel;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InviteRequest {
    @NotEmpty(message = "Укажите хотя бы один номер телефона!")
    private List<
            @NotNull(message = "Номер телефона не может быть null")
            @Pattern(regexp = "^\\+?7\\d{10}$", message = "Неверный номер!")
                    String> phones;
}
