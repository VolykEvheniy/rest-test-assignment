package com.evheniy.testassignment;

import com.evheniy.testassignment.dto.UserDateRangeDto;
import com.evheniy.testassignment.dto.UserRequestDto;
import com.evheniy.testassignment.dto.UserResponseDto;
import com.evheniy.testassignment.dto.UserUpdateFieldsDto;
import com.evheniy.testassignment.exception.EmailAlreadyExistsException;
import com.evheniy.testassignment.exception.InvalidDateRangeException;
import com.evheniy.testassignment.exception.UserLowAgeException;
import com.evheniy.testassignment.exception.UserNotFoundException;
import com.evheniy.testassignment.model.User;
import com.evheniy.testassignment.repository.UserRepository;
import com.evheniy.testassignment.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Value("${user.min-age")
    private int minAge;

    private UserRequestDto userRequestDto;
    private User user;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("user@example.com");
        userRequestDto.setFirstName("First");
        userRequestDto.setLastName("Last");
        userRequestDto.setBirthDate(LocalDate.of(2000, 1, 1));
        userRequestDto.setAddress("1234 Street");
        userRequestDto.setPhoneNumber("1234567890");

        user = new User();
        user.setId(1L);
        user.setEmail(userRequestDto.getEmail());
        user.setFirstName(userRequestDto.getFirstName());
        user.setLastName(userRequestDto.getLastName());
        user.setBirthDate(userRequestDto.getBirthDate());
        user.setAddress(userRequestDto.getAddress());
        user.setPhoneNumber(userRequestDto.getPhoneNumber());

        userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(user.getEmail());
        userResponseDto.setFirstName(user.getFirstName());
        userResponseDto.setLastName(user.getLastName());

        ReflectionTestUtils.setField(userService, "minAge", 18);
    }

    @Test
    void createUser_whenUserDoesNotExist_createsUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        UserResponseDto createdUser = userService.createUser(userRequestDto);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenUserExists_throwsException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(userRequestDto);
        });
    }

    @Test
    void createUser_whenUserIsTooYoung_throwsUserLowAgeException() {

        userRequestDto.setBirthDate(LocalDate.of(2022, 5, 12));

        when(userRepository.existsByEmail(anyString())).thenReturn(false);


        assertThrows(UserLowAgeException.class, () -> userService.createUser(userRequestDto),
                "User must be at least " + minAge + " years old.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_whenUserExists_updatesUser() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        UserResponseDto updatedUser = userService.updateUser(1L, userRequestDto);

        assertNotNull(updatedUser);
        assertEquals(user.getEmail(), updatedUser.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_whenUserDoesNotExist_throwsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(1L, userRequestDto);
        });
    }

    @Test
    void updateUserFields_partialUpdate_changesOnlySpecifiedFields() {
        UserUpdateFieldsDto fieldsDto = new UserUpdateFieldsDto();
        fieldsDto.setFirstName("UpdatedFirstName");

        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setFirstName("FirstName");
        existingUser.setLastName("LastName");
        existingUser.setBirthDate(LocalDate.of(1990, 1, 1));
        existingUser.setId(1L);

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        UserResponseDto updatedUser = userService.updateUserFields(1L, fieldsDto);

        assertNotNull(updatedUser);
        assertEquals("UpdatedFirstName", existingUser.getFirstName());
        verify(userRepository).save(existingUser);
    }

    @Test
    void removeUser_whenUserExists_removesUser() {
        when(userRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(userRepository).deleteById(anyLong());

        userService.removeUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void removeUser_whenUserDoesNotExist_throwsException() {
        when(userRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> {
            userService.removeUser(1L);
        });
    }

    @Test
    void findUsersByBirthDateRange_validRange_returnsUsers() {
        when(userRepository.findByBirthDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(user));
        when(modelMapper.map(any(User.class), eq(UserResponseDto.class))).thenReturn(userResponseDto);

        List<UserResponseDto> users = userService.findUsersByBirthDateRange(new UserDateRangeDto(LocalDate.now().minusDays(10), LocalDate.now()));

        assertFalse(users.isEmpty());
        assertEquals(1, users.size());
    }

    @Test
    void findUsersByBirthDateRange_invalidRange_throwsException() {
        UserDateRangeDto dateRangeDto = new UserDateRangeDto(LocalDate.now(), LocalDate.now().minusDays(1));

        assertThrows(InvalidDateRangeException.class, () -> {
            userService.findUsersByBirthDateRange(dateRangeDto);
        });
    }

}
