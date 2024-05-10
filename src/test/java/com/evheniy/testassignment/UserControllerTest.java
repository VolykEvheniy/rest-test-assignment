package com.evheniy.testassignment;

import com.evheniy.testassignment.controller.UserController;
import com.evheniy.testassignment.dto.UserDateRangeDto;
import com.evheniy.testassignment.dto.UserRequestDto;
import com.evheniy.testassignment.dto.UserResponseDto;
import com.evheniy.testassignment.dto.UserUpdateFieldsDto;
import com.evheniy.testassignment.service.UserService;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.BDDMockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserRequestDto userDto;
    private UserResponseDto userResponseDto;
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        userDto = new UserRequestDto();
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));

        userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(userDto.getEmail());
        userResponseDto.setFirstName(userDto.getFirstName());
        userResponseDto.setLastName(userDto.getLastName());

        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {

        given(userService.createUser(any(UserRequestDto.class))).willReturn(userResponseDto);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {

        given(userService.updateUser(anyLong(), any(UserRequestDto.class))).willReturn(userResponseDto);

        mockMvc.perform(put("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userDto.getEmail()))
                .andExpect(jsonPath("$.firstName").value(userDto.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(userDto.getLastName()));
    }

    @Test
    void updateUserFields_ShouldReturnUpdatedUser() throws Exception {
        UserUpdateFieldsDto fieldsDto = new UserUpdateFieldsDto();
        UserResponseDto userResponseDto = new UserResponseDto();
        given(userService.updateUserFields(anyLong(), any(UserUpdateFieldsDto.class))).willReturn(userResponseDto);

        mockMvc.perform(patch("/api/user/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(fieldsDto)))
                .andExpect(status().isOk());
    }

    @Test
    void removeUser_ShouldReturnSuccessMessage() throws Exception {
        doNothing().when(userService).removeUser(anyLong());

        mockMvc.perform(delete("/api/user/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("User was deleted successfully"));
    }

    @Test
    void getUsersByBirthDateRange_ShouldReturnUsersList() throws Exception {
        UserDateRangeDto userDateRangeDto = new UserDateRangeDto();

        userDateRangeDto.setStartDate(LocalDate.of(2020, 1, 1));
        userDateRangeDto.setEndDate(LocalDate.of(2020, 12, 31));

        List<UserResponseDto> users = Arrays.asList(new UserResponseDto(), new UserResponseDto());
        given(userService.findUsersByBirthDateRange(any(UserDateRangeDto.class))).willReturn(users);

        mockMvc.perform(post("/api/user/_search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDateRangeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(users.size())));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        UserRequestDto userDto = new UserRequestDto();
        userDto.setEmail("invalid-email");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthDate(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void createUser_WithPastDate_ShouldReturnBadRequest() throws Exception {
        UserRequestDto userDto = new UserRequestDto();
        userDto.setEmail("test@example.com");
        userDto.setFirstName("Test");
        userDto.setLastName("User");
        userDto.setBirthDate(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }
}
