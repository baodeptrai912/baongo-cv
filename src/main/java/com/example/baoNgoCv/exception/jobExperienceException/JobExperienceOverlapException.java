package com.example.baoNgoCv.exception.jobExperienceException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JobExperienceOverlapException extends RuntimeException {
    public JobExperienceOverlapException(String message) {
        super(message);
    }
}