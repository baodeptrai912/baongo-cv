package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.entity.Applicant;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.service.domainService.ApplicantService;
import com.example.baoNgoCv.service.domainService.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Main export service implementation that orchestrates the complete export workflow.
 * Handles user authentication, data retrieval, and delegates to format-specific services.
 */
@Service
@Slf4j
public class ExportServiceImpl implements ExportService {

    @Autowired
    private ApplicantService applicantService;

    @Autowired
    private CSVExportService csvExportService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private PDFExportService pdfExportService;

    @Autowired
    private UserService userService;

    /**
     * Exports all job applications for the current authenticated user.
     * Retrieves user from security context, fetches their applications,
     * and delegates file generation to the appropriate format service.
     *
     * @param format Export format: "csv", "excel", or "pdf" (case-insensitive)
     * @return Generated export file with metadata
     * @throws IllegalStateException if no applications found for user
     * @throws IllegalArgumentException if unsupported format provided
     * @throws RuntimeException if export process fails
     */
    @Override
    public ExportResult exportMyApplications(String format) {

        // Get current authenticated user (Jobseeker)
        User user = userService.getCurrentUser();

        // Retrieve all applications for this user
        List<Applicant> applications = applicantService.getApplicantByUser(user);

        if (applications.isEmpty()) {
            throw new IllegalStateException("No applications found to export.");
        }

        // Delegate to appropriate format service
        return generateExport(applications, format);
    }

    /**
     * Exports all applicants for a specific job posting, intended for a Company user.
     * It includes a security check to ensure the company owns the job posting.
     *
     * @param jobPostingId The ID of the job posting.
     * @param format       The desired export format ("csv", "excel", "pdf").
     * @return The generated export file as an ExportResult.
     */
    @Override
    public ExportResult exportApplicantsForJob(Long jobPostingId, String format) {
        // Get current authenticated company
        Company company = userService.getCurrentCompany();

        // Security Check: Ensure the company owns this job posting
        boolean ownsJob = company.getJobs().stream()
                .anyMatch(jp -> Objects.equals(jp.getId(), jobPostingId));

        if (!ownsJob) {
            throw new SecurityException("Access Denied: You do not own this job posting.");
        }

        // Retrieve all applicants for this specific job
        List<Applicant> applicants = applicantService.getApplicantsByJobPostingId(jobPostingId);

        if (applicants.isEmpty()) {
            throw new IllegalStateException("No applicants found for this job posting.");
        }

        return generateExport(applicants, format);
    }

    private ExportResult generateExport(List<Applicant> applicants, String format) {
        return switch (format.toLowerCase()) {
            case "csv" -> csvExportService.exportApplicants(applicants);
            case "excel" -> excelExportService.exportApplicants(applicants);
            case "pdf" -> pdfExportService.exportApplicants(applicants);
            default -> throw new IllegalArgumentException("Unsupported export format: " + format);
        };
    }
}
