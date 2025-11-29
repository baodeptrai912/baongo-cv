package com.example.baoNgoCv.exception.educationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EducationOverlapException extends RuntimeException {
    public EducationOverlapException(String message) {
        super(message);
    }

    public EducationOverlapException(String message, Throwable cause) {
        super(message, cause);
    }
}