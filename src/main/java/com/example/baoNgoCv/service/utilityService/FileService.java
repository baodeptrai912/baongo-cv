package com.example.baoNgoCv.service.utilityService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
@Slf4j
@Service
public class FileService {
    private final String uploadDir = Paths.get("uploads").toAbsolutePath().toString();

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        return timestamp + fileExtension;
    }

    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = generateUniqueFileName(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    public String getFileUrl(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "/img/default/defaultProfilePicture.jpg";
        }
        if (fileName.startsWith("/uploads/")) {
            return fileName;
        }
        return "/uploads/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("File URL is null or empty, skip deletion");
            return;
        }

        try {

            String relativePath = fileUrl.startsWith("/uploads/")
                    ? fileUrl.substring("/uploads/".length())
                    : fileUrl;

            Path filePath = Paths.get(uploadDir).resolve(relativePath).normalize();

            if (Files.deleteIfExists(filePath)) {
                log.info("✅ Deleted file: {}", filePath);
            } else {
                log.warn("⚠️ File not found: {}", filePath);
            }
        } catch (IOException e) {
            log.error("❌ Failed to delete file: {}", fileUrl, e);
            throw new RuntimeException("Could not delete file: " + fileUrl, e);
        }
    }


    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            Resource resource =  new UrlResource(filePath.toUri());
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }
    public void safeDeleteOldFile(String oldFileName, String context) {
        if (oldFileName == null || oldFileName.isEmpty()) {
            log.debug("Skip delete - {} filename is null or empty", context);
            return;
        }

        if (isDefaultImage(oldFileName)) {
            log.debug("Skip delete - {} is default image: {}", context, oldFileName);
            return;
        }

        try {
            deleteFile(oldFileName);
            log.info("🗑️ Successfully deleted old {}: {}", context, oldFileName);
        } catch (Exception e) {
            log.warn("⚠️ Failed to delete old {} ({}): {}", context, oldFileName, e.getMessage());

        }
    }

    private boolean isDefaultImage(String fileName) {
        return fileName.startsWith("/img/default/") ||
                fileName.contains("default") ||
                fileName.equals("defaultProfilePicture.jpg") ||
                fileName.equals("default-company-logo.png");
    }

    // Thêm method này vào FileService
    public String uploadCV(MultipartFile file, Long userId) {
        // Validate file trước
        validateCVFile(file);

        // Lưu file (tái sử dụng logic cũ)
        String fileName = storeFile(file);

        // Trả về URL luôn (1 shot ăn ngay)
        return getFileUrl(fileName);
    }

    // Method validate để đảm bảo file hợp lệ
    private void validateCVFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("CV file is empty");
        }

        // Giới hạn size 10MB
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("CV file exceeds 10MB");
        }

        // Chỉ cho phép PDF, DOC, DOCX
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("Invalid CV file type");
        }

        boolean isValidType = contentType.equals("application/pdf") ||
                contentType.equals("application/msword") ||
                contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

        if (!isValidType) {
            throw new RuntimeException("Only PDF, DOC, DOCX files are allowed");
        }
    }

}
