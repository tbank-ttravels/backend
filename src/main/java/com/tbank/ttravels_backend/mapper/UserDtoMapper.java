package com.tbank.ttravels_backend.mapper;

import com.tbank.ttravels_backend.dto.debt.UserDTO;
import com.tbank.ttravels_backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public UserDTO createUserDTO(User user) {

        return UserDTO.builder()
                .surname(user.getSurname())
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
