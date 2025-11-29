package com.example.baoNgoCv.event.company;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PutInformationRequestEvent {

    private Long companyId;

    @CreatedDate
    private LocalDateTime occurredAt;

    public PutInformationRequestEvent(Long companyId) {
        this.companyId = companyId;
    }
}
