package com.springboot.BankingSystem.exception;

public class InActiveUserException extends RuntimeException {
    public InActiveUserException(String message) {
        super(message);
    }
}
