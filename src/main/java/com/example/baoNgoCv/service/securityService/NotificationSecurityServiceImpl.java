package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.jpa.repository.NotificationRepository;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class NotificationSecurityServiceImpl implements NotificationSecurityService{
    private final NotificationRepository notificationRepository;


    @Override
    public boolean isOwner(Long notificationId) {
        // 1. Lấy thông tin xác thực của người dùng hiện tại từ SecurityContext.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. Nếu không có thông tin xác thực hoặc người dùng chưa đăng nhập, trả về false.
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        // 3. Tìm thông báo trong cơ sở dữ liệu bằng ID được cung cấp.
        Notification notification = notificationRepository.findById(notificationId)
                .orElse(null);

        // 4. Nếu không tìm thấy thông báo, trả về false vì không thể xác định quyền sở hữu.
        if (notification == null) {
            return false;
        }

        // 5. Lấy username của người dùng đang đăng nhập.
        UserDetails userDetail = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetail.getUsername();

        // 6. Kiểm tra xem người dùng hiện tại có phải là người nhận (User) của thông báo không.
        boolean isUserOwner = notification.getRecipientUser() != null &&
                notification.getRecipientUser().getUsername().equals(currentUsername);

        // 7. Kiểm tra xem người dùng hiện tại có phải là người nhận (Company) của thông báo không.
        boolean isCompanyOwner = notification.getRecipientCompany() != null &&
                notification.getRecipientCompany().getUsername().equals(currentUsername);

        // 8. Trả về true nếu người dùng là chủ sở hữu theo một trong hai trường hợp trên.
        return isUserOwner || isCompanyOwner;
    }
}
