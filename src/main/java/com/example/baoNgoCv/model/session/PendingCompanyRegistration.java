package com.example.baoNgoCv.model.session;

import com.example.baoNgoCv.model.dto.company.PostRegisterRequest;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

/**
 * A session-scoped bean to hold company registration data between the initial
 * request and the final email verification step.
 */
@Component
@SessionScope
@Getter
public class PendingCompanyRegistration implements Serializable {

    private PostRegisterRequest pendingRequest;

    public void storeRegistrationData(PostRegisterRequest request) {
        this.pendingRequest = request;
    }

    public boolean isEmpty() {
        return this.pendingRequest == null;
    }

    public boolean isEmailMatching(String email) {
        return this.pendingRequest != null && this.pendingRequest.contactEmail().equalsIgnoreCase(email);
    }

    public void clearPendingData() {
        this.pendingRequest = null;
    }
}