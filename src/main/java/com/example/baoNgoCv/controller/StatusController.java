package com.example.baoNgoCv.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/status")
public class StatusController {

    @GetMapping("/account-deleted-confirmation")
    public String accountDeletedConfirmation() {
        return "status/account-deleted-confirmation";
    }
}
