package com.example.baoNgoCv.model.dto.user;

import java.time.LocalDate;

public record PostEducationResponse(
        Long id,
        String degree,
        String institution,
        LocalDate startDate,
        LocalDate endDate,
        String notes
) {}