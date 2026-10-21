package com.springboot.BankingSystem.config;

import com.springboot.BankingSystem.dto.response.ErrorDto;
import com.springboot.BankingSystem.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(
                                e.getMessage(),
                                "ID not found in Database",
                                Instant.now()
                        )
                );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String >> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        Map<String, String> map = new HashMap<>();
        BindingResult bindingResult = e.getBindingResult();
        List<FieldError> list = bindingResult.getFieldErrors();
        list.forEach(fieldError -> map.put(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(map);
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Logged-in User cannot perform this operation",
                                Instant.now())
                );
    }

    @ExceptionHandler(InvalidAccountStateException.class)
    public ResponseEntity<ErrorDto> handleInvalidAccountStateException(
            InvalidAccountStateException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Can't perform this operation, Account state mismatched!",
                                Instant.now())
                );
    }

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ErrorDto> handleDuplicateRequestException(
            DuplicateRequestException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Duplicate request not allowed",
                                Instant.now())
                );
    }

    @ExceptionHandler(IncompleteVerificationException.class)
    public ResponseEntity<ErrorDto> handleIncompleteVerificationException(
            IncompleteVerificationException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "KYC must be completed",
                                Instant.now())
                );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgumentException(
            IllegalArgumentException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Unexpected argument",
                                Instant.now())
                );
    }

    @ExceptionHandler(EligibilityException.class)
    public ResponseEntity<ErrorDto> handleAgeEligibilityException(
            EligibilityException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Eligibility criteria not satisfied",
                                Instant.now())
                );
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorDto> handleInsufficientBalanceException(
            InsufficientBalanceException e
    ){
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Check balance before debit",
                                Instant.now())
                );
    }
}
