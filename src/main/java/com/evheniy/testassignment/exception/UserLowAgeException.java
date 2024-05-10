package com.evheniy.testassignment.exception;

public class UserLowAgeException extends RuntimeException {

    public UserLowAgeException(String message) {
        super(message);
    }
}
