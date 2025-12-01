package com.tbank.ttravels_backend.dto.debt;

import lombok.*;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDTO {

    private Long id;
    private String name;
    private String surname;
}
