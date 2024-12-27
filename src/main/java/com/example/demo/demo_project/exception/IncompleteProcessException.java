package com.example.demo.demo_project.exception;

public class IncompleteProcessException extends RuntimeException{
    public IncompleteProcessException(String message) {
        super(message);
    }
    public IncompleteProcessException(String message, Exception e) {
        super(message + "Exception: " + e.getMessage());
    }
}
