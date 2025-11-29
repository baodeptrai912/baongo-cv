package com.example.baoNgoCv.exception.jobExperienceException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidJobDateException extends RuntimeException {
    public InvalidJobDateException(String message) {
        super(message);
    }
}