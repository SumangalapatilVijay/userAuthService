package com.example.authservice.exception;

public class UserAlreadyExistException extends Exception {
    public UserAlreadyExistException(String s) {
        super(s);
    }
}
