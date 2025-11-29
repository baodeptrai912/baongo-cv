package com.example.baoNgoCv.model.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PutContactRequest {

    @NotBlank(message = "{company.name.required}")
    @Size(min = 2, max = 100, message = "{company.name.size}")
    private String companyName;

    @NotBlank(message = "{company.location.required}")
    @Size(min = 5, max = 200, message = "{company.location.size}")
    private String location;

    @URL(message = "{company.website.url}")
    @Size(max = 255, message = "{company.website.size}")
    private String website;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,15}$", message = "{company.phone.pattern}")
    @Size(max = 20, message = "{company.phone.size}")
    private String contactPhone;
}
