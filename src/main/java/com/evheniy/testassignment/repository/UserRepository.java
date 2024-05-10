package com.evheniy.testassignment.repository;

import com.evheniy.testassignment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    List<User> findByBirthDateBetween(LocalDate start, LocalDate end);
}
