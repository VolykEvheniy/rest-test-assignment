package com.evheniy.testassignment.service;

import com.evheniy.testassignment.dto.UserDateRangeDto;
import com.evheniy.testassignment.dto.UserRequestDto;
import com.evheniy.testassignment.dto.UserResponseDto;
import com.evheniy.testassignment.dto.UserUpdateFieldsDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(UserRequestDto userDto);

    UserResponseDto updateUser(Long id, UserRequestDto userDto);

    UserResponseDto updateUserFields(Long id, UserUpdateFieldsDto userDto);

    void removeUser(Long id);

    List<UserResponseDto> findUsersByBirthDateRange(UserDateRangeDto dateRangeDto);
}
