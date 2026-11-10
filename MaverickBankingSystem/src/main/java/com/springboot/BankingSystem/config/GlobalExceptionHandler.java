package com.springboot.BankingSystem.config;

import com.springboot.BankingSystem.dto.response.ErrorDto;
import com.springboot.BankingSystem.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.warn("Id not found in DB");
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
        list.forEach(fieldError -> {
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
            log.warn("Field {} , failed validation: {}", fieldError.getField(), fieldError.getDefaultMessage());
        });
        return ResponseEntity
                .badRequest()
                .body(map);
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ){
        log.warn("Unauthorized Access");
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
        log.warn("Invalid Account State");
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
        log.warn("Duplicate Request");
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
        log.warn("Incomplete KYC");
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
        log.warn("Invalid input");
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
        log.warn("Not eligible");
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
        log.warn("Insufficient balance");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Check balance before debit",
                                Instant.now())
                );
    }

    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorDto> handlePasswordMismatchException(
            PasswordMismatchException e
    ){
        log.warn("Incorrect password");
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Password should be matched",
                                Instant.now())
                );
    }

    @ExceptionHandler(InActiveUserException.class)
    public ResponseEntity<ErrorDto> handleInActiveUserException(
            InActiveUserException e
    ) {
        log.warn("User is inactive");
        return ResponseEntity
                .status(HttpStatus.PRECONDITION_FAILED)
                .body(new ErrorDto(
                        e.getMessage(),
                        "Contact Admin at admin@helpdesk.com",
                        Instant.now()
                ));
    }
}
