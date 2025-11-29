package com.example.baoNgoCv.event.company;

import com.example.baoNgoCv.model.enums.AccountTier;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubscriptionDowngradedEvent extends ApplicationEvent {
    private final Long companyId;
    private final String companyName;
    private final String companyEmail;
    private final AccountTier oldTier;

    public SubscriptionDowngradedEvent(Object source, Long companyId, String companyName, String companyEmail, AccountTier oldTier) {
        super(source);
        this.companyId = companyId;
        this.companyName = companyName;
        this.companyEmail = companyEmail;
        this.oldTier = oldTier;
    }
}
