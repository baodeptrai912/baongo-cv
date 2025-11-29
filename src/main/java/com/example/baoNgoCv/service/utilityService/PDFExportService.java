package com.example.baoNgoCv.service.utilityService;

import com.example.baoNgoCv.model.dto.applicant.ExportResult;
import com.example.baoNgoCv.model.entity.Applicant;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF export service for converting job applications to PDF format.
 * Creates formatted PDF document with title, application details and styling.
 */
@Service
@Slf4j
public class PDFExportService implements FileExportService {

    /**
     * Converts application list to PDF format with title and formatted content.
     *
     * @param applications List of applications to export
     * @return PDF file as ExportResult with formatted layout
     * @throws RuntimeException if PDF generation fails
     */
    @Override
    public ExportResult exportApplicants(List<Applicant> applications) {
        try {
            Document document = new Document();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter.getInstance(document, outputStream);
            document.open();

            addTitle(document);
            addApplications(document, applications);

            document.close();

            String fileName = generateFileName();

            return ExportResult.builder()
                    .fileName(fileName)
                    .data(outputStream.toByteArray())
                    .contentType(getContentType())
                    .build();

        } catch (DocumentException e) {
            log.error("Error generating PDF export: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF export", e);
        }
    }

    /**
     * @return PDF MIME type "application/pdf"
     */
    @Override
    public String getContentType() {
        return "application/pdf";
    }

    /**
     * @return PDF file extension "pdf"
     */
    @Override
    public String getFileExtension() {
        return "pdf";
    }

    /**
     * Adds centered title "My Job Applications" to PDF document.
     *
     * @param document PDF document to add title to
     * @throws DocumentException if title addition fails
     */
    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("My Job Applications", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));
    }

    /**
     * Adds all application details to PDF document with formatting.
     *
     * @param document PDF document to add applications to
     * @param applications List of applications to format and add
     * @throws DocumentException if content addition fails
     */
    private void addApplications(Document document, List<Applicant> applications) throws DocumentException {
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        for (Applicant app : applications) {
            addApplicationDetails(document, app, boldFont, normalFont);
            document.add(new Paragraph(" "));
        }
    }

    /**
     * Formats and adds single application details with null-safe field extraction.
     *
     * @param document PDF document to add content to
     * @param app Applicant entity to extract data from
     * @param boldFont Font for job titles
     * @param normalFont Font for application details
     * @throws DocumentException if content formatting fails
     */
    private void addApplicationDetails(Document document, Applicant app, Font boldFont, Font normalFont) throws DocumentException {
        Paragraph jobTitle = new Paragraph(
                app.getJobPosting() != null ? app.getJobPosting().getTitle() : "N/A", boldFont);
        Paragraph company = new Paragraph("Company: " +
                (app.getJobPosting() != null && app.getJobPosting().getCompany() != null
                        ? app.getJobPosting().getCompany().getName() : "N/A"), normalFont);
        Paragraph appliedDate = new Paragraph("Applied: " +
                app.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE), normalFont);
        Paragraph status = new Paragraph("Status: " +
                (app.getCurrentStatusHistory() != null
                        ? app.getCurrentStatusHistory().getStatus().name() : "UNKNOWN"), normalFont);

        document.add(jobTitle);
        document.add(company);
        document.add(appliedDate);
        document.add(status);
    }

    /**
     * Generates timestamped PDF filename.
     *
     * @return Filename format: "applications_YYYY-MM-DD.pdf"
     */
    private String generateFileName() {
        return "applications_" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "." + getFileExtension();
    }
}