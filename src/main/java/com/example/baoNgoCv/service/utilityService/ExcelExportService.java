package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.entity.Applicant;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Excel export service for converting job applications to XLSX format.
 * Creates formatted Excel workbook with styled headers and auto-sized columns.
 */
@Service
@Slf4j
public class ExcelExportService implements FileExportService {

    /**
     * Converts application list to Excel XLSX format with styling and formatting.
     *
     * @param applications List of applications to export
     * @return Excel file as ExportResult with formatted headers and data
     * @throws RuntimeException if Excel generation fails
     */
    @Override
    public ExportResult exportApplicants(List<Applicant> applications) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Job Applications");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);

            // Create headers
            createHeaderRow(sheet, headerStyle);

            // Create data rows
            createDataRows(sheet, applications);

            // Auto-size columns
            autoSizeColumns(sheet);

            // Convert to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            String fileName = generateFileName();

            return ExportResult.builder()
                    .fileName(fileName)
                    .data(outputStream.toByteArray())
                    .contentType(getContentType())
                    .build();

        } catch (IOException e) {
            log.error("Error generating Excel export: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    /**
     * @return Excel MIME type for XLSX files
     */
    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    /**
     * @return Excel file extension "xlsx"
     */
    @Override
    public String getFileExtension() {
        return "xlsx";
    }

    /**
     * Creates styled header with bold white text on dark blue background.
     *
     * @param workbook Excel workbook instance
     * @return Configured cell style for headers
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    /**
     * Creates header row with column titles and applies styling.
     *
     * @param sheet Excel sheet to add headers to
     * @param headerStyle Style to apply to header cells
     */
    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Application ID", "Job Title", "Company Name", "Application Date",
                "Current Status", "Last Updated", "Resume File", "Contact Email"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Creates data rows for all applications starting from row 1.
     *
     * @param sheet Excel sheet to add data to
     * @param applications List of applications to convert to rows
     */
    private void createDataRows(Sheet sheet, List<Applicant> applications) {
        int rowNum = 1;
        for (Applicant app : applications) {
            Row row = sheet.createRow(rowNum++);
            populateDataRow(row, app);
        }
    }

    /**
     * Populates single Excel row with application data using null-safe extraction.
     *
     * @param row Excel row to populate
     * @param app Applicant entity to extract data from
     */
    private void populateDataRow(Row row, Applicant app) {
        row.createCell(0).setCellValue(app.getId());
        row.createCell(1).setCellValue(
                app.getJobPosting() != null ? app.getJobPosting().getTitle() : "N/A");
        row.createCell(2).setCellValue(
                app.getJobPosting() != null && app.getJobPosting().getCompany() != null
                        ? app.getJobPosting().getCompany().getName() : "N/A");
        row.createCell(3).setCellValue(app.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        row.createCell(4).setCellValue(
                app.getCurrentStatusHistory() != null
                        ? app.getCurrentStatusHistory().getStatus().name() : "UNKNOWN");
        row.createCell(5).setCellValue(
                app.getUpdatedAt() != null
                        ? app.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
        row.createCell(6).setCellValue(app.getResume() != null ? app.getResume() : "No file");
        row.createCell(7).setCellValue(app.getUser().getContactInfo().getEmail() != null ? app.getUser().getContactInfo().getEmail() : "");
    }

    /**
     * Auto-sizes all columns to fit content for better readability.
     *
     * @param sheet Excel sheet to auto-size columns for
     */
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Generates timestamped Excel filename.
     *
     * @return Filename format: "applications_YYYY-MM-DD.xlsx"
     */
    private String generateFileName() {
        return "applications_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + getFileExtension();
    }
}
