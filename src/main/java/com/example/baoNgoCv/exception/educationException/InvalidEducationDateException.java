package com.example.baoNgoCv.exception.educationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEducationDateException extends RuntimeException {
    public InvalidEducationDateException(String message) {
        super(message);
    }
}