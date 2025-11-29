package com.example.baoNgoCv.model.dto.company;

import com.example.baoNgoCv.model.enums.IndustryType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PutInformationRequest {


    @NotNull(message = "{industry.required}")
    private IndustryType industry;

    @NotNull(message = "{company.size.required}")
    @Min(value = 1, message = "{company.size.min}")
    @Max(value = 100000, message = "{company.size.max}")
    private Integer companySize;

    @NotBlank(message = "{company.description.required}")
    @Size(min = 10, max = 2000, message = "{company.description.size}")
    private String description;

    private MultipartFile logoFile;

    public boolean hasLogo() {
        return logoFile != null && !logoFile.isEmpty();
    }

    public String getLogoFileName() {
        return hasLogo() ? logoFile.getOriginalFilename() : null;
    }
}
