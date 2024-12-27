package com.example.demo.demo_project.exception;

import java.util.List;

public record Error(
        String errorCode,
        List<String> errorMessages
) {
}
