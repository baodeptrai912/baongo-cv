package com.example.baoNgoCv.model.session;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.example.baoNgoCv.model.dto.user.PostRegisterRequest;

@Data
@NoArgsConstructor
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PendingUserRegistration {

    private String username;
    private String password;
    private String email;

    public void storeRegistrationData(PostRegisterRequest dto) {
        this.username = dto.getUsername();
        this.password = dto.getPassword();
        this.email = dto.getEmail();
    }

    public boolean hasPendingData() {
        return this.username != null && this.password != null && this.email != null;
    }

    public boolean isEmpty() {
        return !hasPendingData();
    }

    public boolean isEmailMatching(String emailToVerify) {
        return this.email != null && this.email.equals(emailToVerify);
    }

    public void clearPendingData() {
        this.username = null;
        this.password = null;
        this.email = null;
    }

    @Override
    public String toString() {
        return "PendingUserRegistration{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", hasPendingData=" + hasPendingData() +
                '}';
    }
}
