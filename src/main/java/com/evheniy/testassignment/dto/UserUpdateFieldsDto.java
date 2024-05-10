package com.evheniy.testassignment.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UserUpdateFieldsDto {

    @Email(message = "Invalid email format")
    private String email;

    private String firstName;

    private String lastName;

    @Past(message = "Birth date must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDate;

    private String address;

    private String phoneNumber;
}
