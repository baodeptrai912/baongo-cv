package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;

/**
 * Main service for exporting user's job applications.
 * Handles user authentication, data retrieval, and coordinates with format-specific services.
 */
public interface ExportService {

    /**
     * Exports all applications for current user in specified format.
     *
     * @param format Export format: "csv", "excel", or "pdf"
     * @return Generated file with metadata
     * @throws IllegalStateException if no applications found
     */
    ExportResult exportMyApplications(String format);

    /**
     * Exports all applicants for a specific job posting.
     * This is intended for the Company role.
     *
     * @param jobPostingId The ID of the job posting.
     * @param format Export format: "csv", "excel", or "pdf".
     * @return Generated file with metadata.
     */
    ExportResult exportApplicantsForJob(Long jobPostingId, String format);
}
