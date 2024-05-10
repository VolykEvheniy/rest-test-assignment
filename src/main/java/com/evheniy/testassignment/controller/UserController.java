package com.evheniy.testassignment.controller;

import com.evheniy.testassignment.dto.UserDateRangeDto;
import com.evheniy.testassignment.dto.UserRequestDto;
import com.evheniy.testassignment.dto.UserResponseDto;
import com.evheniy.testassignment.dto.UserUpdateFieldsDto;
import com.evheniy.testassignment.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userDto) {
        UserResponseDto userResponseDto = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserRequestDto userDto) {
        UserResponseDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUserFields(@PathVariable Long id, @Valid @RequestBody UserUpdateFieldsDto userDto) {
        UserResponseDto updatedUser = userService.updateUserFields(id, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeCar(@PathVariable Long id) {
        userService.removeUser(id);
        return ResponseEntity.ok("User was deleted successfully");
    }

    @PostMapping("/_search")
    public ResponseEntity<List<UserResponseDto>> getUsersByBirthDateRange(@Valid @RequestBody UserDateRangeDto dateRangeDto) {
        List<UserResponseDto> users = userService.findUsersByBirthDateRange(dateRangeDto);
        return ResponseEntity.ok(users);
    }



}
