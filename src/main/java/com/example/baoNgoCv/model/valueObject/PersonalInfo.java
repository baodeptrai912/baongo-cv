package com.example.baoNgoCv.model.valueObject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.Period;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInfo {
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    public int getAge() {
        return dateOfBirth != null ? Period.between(dateOfBirth, LocalDate.now()).getYears() : 0;
    }

    public boolean isAdult() {
        return getAge() >= 18;
    }
}
