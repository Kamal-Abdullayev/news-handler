package com.example.demo.demo_project.exception;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GeneralExceptionAdvice {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<CustomErrorResponse> handle() {
        return new ResponseEntity<>(new CustomErrorResponse(
                new Error("E1002",
                        List.of("Request limit excited!"))
                ), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomErrorResponse> handle(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new CustomErrorResponse(
                new Error("E1000",
                        List.of(exception.getMessage())
        )), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IncompleteProcessException.class)
    public ResponseEntity<CustomErrorResponse> handle(IncompleteProcessException exception) {
        return new ResponseEntity<>(new CustomErrorResponse(
                new Error("E1001",
                        List.of(exception.getMessage()))
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();

        return new ResponseEntity<>(new CustomErrorResponse(
                new Error("E1003", errorMessages)
        ), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpMessageNotReadableException() {
        return new ResponseEntity<>(new CustomErrorResponse(
                new Error("E1005", List.of("Required request body is missing or unreadable"))
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex) {


        return new ResponseEntity<>(new CustomErrorResponse(
                new Error(
                        "E1005",
                        List.of("Request method '" + ex.getMethod() + "' not supported")
                )
        ), HttpStatus.METHOD_NOT_ALLOWED);
    }




}
