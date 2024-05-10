package com.evheniy.testassignment.dto;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String birthDate;

}
