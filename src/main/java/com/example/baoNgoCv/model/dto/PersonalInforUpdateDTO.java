package com.example.baoNgoCv.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
public class PersonalInforUpdateDTO {

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email should be valid.")
    private String email;

    @NotBlank(message = "Phone number is required.")
    private String phone;

    @NotBlank(message = "Location is required.")
    private String location;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @NotNull(message = "Date of Birth is required.")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Nationality is required.")
    private String nationality;

    @NotNull(message = "Gender is required.")
    private String gender;

    private MultipartFile avatar;
}
