package com.evheniy.testassignment.service;

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
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Value("${user.min-age}")
    private int minAge;


    @Transactional
    @Override
    public UserResponseDto createUser(UserRequestDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new EmailAlreadyExistsException("This email already exists: " + userDto.getEmail());
        }
        if (Period.between(userDto.getBirthDate(), LocalDate.now()).getYears() < minAge) {
            throw new UserLowAgeException("User must be at least " + minAge + " years old.");
        }
        User user = buildUser(userDto.getEmail(), userDto.getFirstName(),
                userDto.getLastName(), userDto.getAddress(), userDto.getPhoneNumber(), userDto.getBirthDate());

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(Long id, UserRequestDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " was not found"));

        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setAddress(userDto.getAddress());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setBirthDate(userDto.getBirthDate());

        return modelMapper.map(userRepository.save(user), UserResponseDto.class);
    }

    private User buildUser(String email, String firstName, String lastName, String address, String phoneNumber, LocalDate birthDate) {
        User user = new User();

        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAddress(address);
        user.setPhoneNumber(phoneNumber);
        user.setBirthDate(birthDate);

        return user;
    }

    @Transactional
    @Override
    public UserResponseDto updateUserFields(Long id, UserUpdateFieldsDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID: " + id + " was not found"));

        if (userDto.getEmail() != null) user.setEmail(userDto.getEmail());
        if (userDto.getFirstName() != null) user.setFirstName(userDto.getFirstName());
        if (userDto.getLastName() != null) user.setLastName(userDto.getLastName());
        if (userDto.getBirthDate() != null) user.setBirthDate(userDto.getBirthDate());
        if (userDto.getAddress() != null) user.setAddress(userDto.getAddress());
        if (userDto.getPhoneNumber() != null) user.setPhoneNumber(userDto.getPhoneNumber());

        userRepository.save(user);

        return modelMapper.map(user, UserResponseDto.class);
    }

    @Transactional
    @Override
    public void removeUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with ID: " + id + " was not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<UserResponseDto> findUsersByBirthDateRange(UserDateRangeDto dateRangeDto) {
        if (dateRangeDto.getStartDate().isAfter(dateRangeDto.getEndDate())) {
            throw new InvalidDateRangeException("Start date must be before end date");
        }

        return userRepository.findByBirthDateBetween(dateRangeDto.getStartDate(), dateRangeDto.getEndDate()).stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());
    }
}
