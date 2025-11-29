package com.example.baoNgoCv.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        log.error("Error occurred - Status: {}, URI: {}",
                status, request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            switch (statusCode) {
                case 401: return "status/401";
                case 404: return "status/404";
                case 403: return "status/403";
                case 500: return "status/500";
                default: return "status/error";
            }
        }

        return "status/error";
    }
}