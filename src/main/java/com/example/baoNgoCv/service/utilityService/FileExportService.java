package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.entity.Applicant;
import java.util.List;

/**
 * Interface for converting application data into specific file formats.
 * Each implementation handles one format (CSV, Excel, PDF).
 */
public interface FileExportService {

    /**
     * Converts application list to file format.
     *
     * @param applications List of applications to export
     * @return Generated file with data and metadata
     */
    ExportResult exportApplicants(List<Applicant> applications);

    /**
     * @return MIME type for this format (e.g., "text/csv")
     */
    String getContentType();

    /**
     * @return File extension for this format (e.g., "csv")
     */
    String getFileExtension();
}
