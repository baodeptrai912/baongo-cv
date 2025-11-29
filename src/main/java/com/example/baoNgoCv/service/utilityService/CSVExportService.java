package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.entity.Applicant;
import com.opencsv.CSVWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV export service for converting job applications to CSV format.
 * Generates comma-separated values file with application data and headers.
 */
@Service
@Slf4j
public class CSVExportService implements FileExportService {

    /**
     * Converts application list to CSV format with headers and data rows.
     *
     * @param applications List of applications to export
     * @return CSV file as ExportResult with UTF-8 encoding
     * @throws RuntimeException if CSV generation fails
     */
    @Override
    public ExportResult exportApplicants(List<Applicant> applications) {
        try {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);

            // CSV Headers
            String[] headers = {
                    "Application ID", "Job Title", "Company Name", "Application Date",
                    "Current Status", "Last Updated", "Resume File", "Contact Email"
            };
            csvWriter.writeNext(headers);

            // CSV Data Rows
            for (Applicant app : applications) {
                String[] row = buildDataRow(app);
                csvWriter.writeNext(row);
            }

            csvWriter.close();

            String fileName = generateFileName();
            byte[] data = stringWriter.toString().getBytes(StandardCharsets.UTF_8);

            return ExportResult.builder()
                    .fileName(fileName)
                    .data(data)
                    .contentType(getContentType())
                    .build();

        } catch (IOException e) {
            log.error("Error generating CSV export: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    /**
     * @return CSV MIME type "text/csv"
     */
    @Override
    public String getContentType() {
        return "text/csv";
    }

    /**
     * @return CSV file extension "csv"
     */
    @Override
    public String getFileExtension() {
        return "csv";
    }

    /**
     * Builds CSV data row from Applicant entity with null-safe field extraction.
     *
     * @param app Applicant entity to convert
     * @return String array representing CSV row data
     */
    private String[] buildDataRow(Applicant app) {
        return new String[]{
                String.valueOf(app.getId()),
                app.getJobPosting() != null ? app.getJobPosting().getTitle() : "N/A",
                app.getJobPosting() != null && app.getJobPosting().getCompany() != null
                        ? app.getJobPosting().getCompany().getName() : "N/A",
                app.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE),
                app.getCurrentStatusHistory() != null
                        ? app.getCurrentStatusHistory().getStatus().name() : "UNKNOWN",
                app.getUpdatedAt() != null
                        ? app.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "",
                app.getResume() != null ? app.getResume() : "No file",
                app.getUser().getContactInfo().getEmail() != null ? app.getUser().getContactInfo().getEmail() : ""
        };
    }

    /**
     * Generates timestamped CSV filename.
     *
     * @return Filename format: "applications_YYYY-MM-DD.csv"
     */
    private String generateFileName() {
        return "applications_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + getFileExtension();
    }
}
