package com.tbank.ttravels_backend.dto;

import lombok.Getter;

@Getter
public class RegisterRequest {

    private String phone;
    private String name;
    private String surname;
    private String password;

}