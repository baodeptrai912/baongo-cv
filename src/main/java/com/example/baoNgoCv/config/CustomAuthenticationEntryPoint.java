package com.example.baoNgoCv.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {


    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {



        // Check if it's AJAX request
        if (isAjaxRequest(request)) {
            // Return JSON response for AJAX
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNAUTHORIZED,
                    "You need to login to use this feature!"
            );

        } else {
            response.sendRedirect("/user/login");
        }
    }

    private boolean isPermitAllEndpoint(String requestURI) {
        return requestURI.startsWith("/user/send-email-code-forget-password/") &&
                requestURI.length() > "/user/send-email-code-forget-password/".length() ||
                requestURI.equals("/user/register") ||
                requestURI.equals("/user/check-username");
    }


    private boolean isAjaxRequest(HttpServletRequest request) {
        // Method 1: Check X-Requested-With header
        String xRequestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            return true;
        }

        // Method 2: Check Accept header
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return true;
        }

        // Method 3: Check Content-Type
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            return true;
        }

        return false;
    }
}