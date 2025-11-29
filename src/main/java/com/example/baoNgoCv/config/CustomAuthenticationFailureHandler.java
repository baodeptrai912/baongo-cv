package com.example.baoNgoCv.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String username = request.getParameter("username");
        if (username != null && !username.isEmpty()) {
            request.getSession().setAttribute("LAST_USERNAME", username);
        }

        setDefaultFailureUrl("/user/login?error=true");
        super.onAuthenticationFailure(request, response, exception);
    }
}
