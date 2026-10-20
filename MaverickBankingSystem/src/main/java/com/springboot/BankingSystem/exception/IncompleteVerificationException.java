package com.springboot.BankingSystem.exception;

public class IncompleteVerificationException extends RuntimeException {
    public IncompleteVerificationException(String message) {
        super(message);
    }
}
