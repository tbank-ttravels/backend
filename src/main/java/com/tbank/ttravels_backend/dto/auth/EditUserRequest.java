package com.tbank.ttravels_backend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Данные для изменения имени и/или фамилии")
public class EditUserRequest {
    @Schema(description = "Новое имя пользователя",
            example = "Василий")
    private String newName;

    @Schema(description = "Новая фамилия пользователя",
            example = "Василий")
    private String newSurname;
}
