package com.java.toystore.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(AccessDeniedException ex) {
        log.error("AccessDeniedException: ", ex);
        return new  ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ToyNotFoundException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(ToyNotFoundException ex) {
        log.error("ToyNotFoundException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(OrderNotFoundException ex) {
        log.error("OrderNotFoundException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(InsufficientStockException ex) {
        log.error("InsufficientStockException: ", ex);
        return new ResponseEntity<>(new ApplicationErrorResponse(ex), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TaskRejectedException.class)
    public ResponseEntity<ApplicationErrorResponse> handle(TaskRejectedException ex) {
        log.warn("Async task rejected: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApplicationErrorResponse("Async processing is busy. Please try again later."),
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }
}
