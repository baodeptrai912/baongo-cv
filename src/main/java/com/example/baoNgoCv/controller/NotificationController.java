package com.example.baoNgoCv.controller;

import com.example.baoNgoCv.model.dto.NotificationAllDTO;
import com.example.baoNgoCv.model.dto.NotificationDTO;
import com.example.baoNgoCv.model.dto.notification.GetAllNotificationResponse;
import com.example.baoNgoCv.model.entity.ApplicationReview;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Notification;
import com.example.baoNgoCv.model.entity.User;
import com.example.baoNgoCv.jpa.repository.ApplicationReviewRepository;
import com.example.baoNgoCv.jpa.repository.NotificationRepository;
import com.example.baoNgoCv.service.domainService.UserService;
import com.example.baoNgoCv.service.utilityService.NotificationService;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final ApplicationReviewRepository applicationReviewRepository;


    @GetMapping("/get-notification")
    public ResponseEntity<Map<String, Object>> getNotification() {
        log.info("🔔 [GET_NOTIFICATION] Received request to fetch notifications.");
        Map<String, Object> response = new HashMap<>();
        List<NotificationDTO> notificationDTOs = new ArrayList<>();

        // ✅ Lấy principal từ SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();

        log.info("🔐 Principal type: {}", principal.getClass().getSimpleName());

        // ✅ Check COMPANY trước
        if (principal instanceof Company) {
            Company company = (Company) principal;
            log.info("🏢 [GET_NOTIFICATION] Fetching notifications for Company: {} (ID: {})",
                    company.getUsername(), company.getId());

            List<Notification> listNoti = notificationRepository.findByRecipientCompany(company);
            log.info("✅ Found {} notifications for company.", listNoti.size());

            if (listNoti.isEmpty()) {
                log.warn("⚠️ No notifications found for company ID: {}", company.getId());
            }

            // ✅ Thủ công convert
            for (Notification notification : listNoti) {
                NotificationDTO dto = new NotificationDTO();
                dto.setId(notification.getId());

                if (notification.getSenderUser() != null) {
                    String fullName = notification.getSenderUser().getPersonalInfo().getFullName();
                    dto.setSender(fullName != null ? fullName : notification.getSenderUser().getUsername());
                } else if (notification.getSenderCompany() != null) {
                    dto.setSender(notification.getSenderCompany().getName());
                } else {
                    dto.setSender("System");
                }

                dto.setRead(notification.isRead());
                dto.setTitle(notification.getTitle());
                dto.setCreatedAt(notification.getCreatedAt());
                dto.setAvatar(notification.getAvatar());
                dto.setHref(notification.getFullHref());
                notificationDTOs.add(dto);
            }

        } else if (principal instanceof User) {
            User user = (User) principal;
            log.info("👤 [GET_NOTIFICATION] Fetching notifications for User: {} (ID: {})",
                    user.getUsername(), user.getId());

            List<Notification> listNoti = notificationRepository.findByRecipientUser(user);
            log.info("✅ Found {} notifications for user.", listNoti.size());

            // ✅ Thủ công convert
            for (Notification notification : listNoti) {
                NotificationDTO dto = new NotificationDTO();
                dto.setId(notification.getId());

                if (notification.getSenderUser() != null) {
                    String fullName = notification.getSenderUser().getPersonalInfo().getFullName();
                    dto.setSender(fullName != null ? fullName : notification.getSenderUser().getUsername());
                } else if (notification.getSenderCompany() != null) {
                    dto.setSender(notification.getSenderCompany().getName());
                } else {
                    dto.setSender("System");
                }

                dto.setRead(notification.isRead());
                dto.setTitle(notification.getTitle());
                dto.setCreatedAt(notification.getCreatedAt());
                dto.setAvatar(notification.getAvatar());
                dto.setHref(notification.getFullHref());
                notificationDTOs.add(dto);
            }

        } else {
            log.error("❌ Unknown principal type: {}", principal.getClass().getName());
            response.put("success", false);
            response.put("error", "Unknown user type");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        log.info("✅ [GET_NOTIFICATION] Successfully prepared {} notifications. Sending response.",
                notificationDTOs.size());
        response.put("success", true);
        response.put("notifications", notificationDTOs);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/applicant-review-detail/{id}")
    public String applicantReviewDetail(@PathVariable("id") Long id,
                                        @RequestParam(value = "notificationId", required = false) Long notificationId,
                                        Model model) {

        if (notificationId != null) {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);

            if (notification != null) {

                notification.setRead(true);

                notificationRepository.save(notification);
            }
        }

        ApplicationReview applicationReview = applicationReviewRepository.findById(id).orElse(null);

        model.addAttribute("applicationReview", applicationReview);

        return "notification/applicant-review-detail";
    }
    @GetMapping("/all")
    public String viewAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "all") String type,
            Model model,
            Authentication authentication) { // ✅ Thêm Authentication parameter

            // 1. Tạo Pageable
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // 2. ✅ Xác định User hoặc Company từ Authentication
            Object principal = authentication.getPrincipal();
            User currentUser = null;
            Company currentCompany = null;

            if (principal instanceof User) {
                currentUser = (User) principal;
                log.info("Loading notifications for User: {}", currentUser.getUsername());
            } else if (principal instanceof Company) {
                currentCompany = (Company) principal;
                log.info("Loading notifications for Company: {}", currentCompany.getName());
            } else {
                throw new RuntimeException("Unknown principal type: " + principal.getClass());
            }

            // 3. Gọi service với currentUser và currentCompany
            GetAllNotificationResponse response = notificationService
                    .getNotificationsForCurrentUser(authentication,status, type, pageable);

            // 4. Lấy available types
            Map<String, Integer> availableTypes = notificationService
                    .getAvailableTypeCounts(currentUser, currentCompany);

            // 5. Add vào model
            model.addAttribute("response", response);
            model.addAttribute("availableTypes", availableTypes);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("currentStatus", status);
            model.addAttribute("currentType", type);

            return "notification/all-notifications";


    }








    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            notificationService.deleteNotification(id);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toggle-read/{notificationId}")
    public ResponseEntity<Map<String, Object>> toggleNotificationReadStatus(
            @PathVariable Long notificationId,
            Authentication authentication) {
System.out.println("Đã nhận");
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            Company company = userService.getCurrentCompany();

            Notification notification = notificationService.toggleReadStatus(notificationId, currentUser, company);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("notificationId", notificationId);
            response.put("isRead", notification.isRead());
            response.put("message", notification.isRead() ? "Marked as read" : "Marked as unread");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/mark-all-as-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMarkAllAsRead(Authentication authentication) {
        try {
            notificationService.markAllAsReadForCurrentUser(authentication);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
