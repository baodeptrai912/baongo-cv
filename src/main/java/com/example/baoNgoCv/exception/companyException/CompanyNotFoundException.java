package com.example.baoNgoCv.exception.companyException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompanyNotFoundException extends RuntimeException {


    public CompanyNotFoundException() {
        super("Your company is not found !");
    }



}
