package com.cbs.CabBookingSystem.dto;


import lombok.Data;

@Data
public class UserLoginDto {

    private String email;

    private String passwordHash;

}
