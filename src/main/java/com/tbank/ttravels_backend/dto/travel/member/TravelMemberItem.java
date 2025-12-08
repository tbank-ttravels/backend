package com.tbank.ttravels_backend.dto.travel.member;

import com.tbank.ttravels_backend.enums.MemberRole;
import com.tbank.ttravels_backend.enums.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Элемент информации о участнике поездки")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TravelMemberItem {
    @Schema(description = "Идентификатор участника поездки", example = "1001")
    private Long id;
    @Schema(description = "Имя участника поездки", example = "Иван Иванов")
    private String name;
    @Schema(description = "Телефон участника поездки", example = "+7-999-123-45-67")
    private String phone;
    @Schema(description = "Статус участника поездки", example = "INVITED")
    private MemberStatus status;
    @Schema(description = "Роль участника в поездке", example = "MEMBER")
    private MemberRole role;
}
