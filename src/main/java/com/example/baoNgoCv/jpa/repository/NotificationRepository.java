package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.jpa.projection.notification.NotificationCountProjection;
import com.example.baoNgoCv.jpa.projection.notification.NotificationProjection;
import com.example.baoNgoCv.model.enums.NotificationType;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Notification;
import com.example.baoNgoCv.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUser(User user);

    List<Notification> findByRecipientCompany(Company company);

    // 1. Tìm notifications theo user với pagination
    Page<Notification> findByRecipientUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Notification> findByRecipientUserAndIsReadOrderByCreatedAtDesc(User user, boolean isRead, Pageable pageable);



    // 4. Đếm số unread notifications
    List<Notification> findByRecipientUserAndIsRead(User user, boolean isRead);

    List<Notification> findByRecipientCompanyAndIsRead(Company company, boolean isRead);

    @Modifying
    @Query(value = "UPDATE notification SET deleted_at = NOW() WHERE applicant_id = :applicantId", nativeQuery = true)
    void softDeleteByApplicantId(@Param("applicantId") Long applicantId);

    // 6. Tìm notifications theo applicant (để xóa khi xóa applicant)
    List<Notification> findByApplicantId(Long applicantId);

    // 7. Xóa notifications theo applicant ID
    void deleteByApplicantId(Long applicantId);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser = :user ORDER BY n.createdAt DESC")
    Page<Notification> findActiveByUser(@Param("user") User user, Pageable pageable);


    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "(:user IS NOT NULL AND n.recipientUser = :user) OR " +
            "(:company IS NOT NULL AND n.recipientCompany = :company)")
    int countByRecipient(@Param("user") User user, @Param("company") Company company);


    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "((:user IS NOT NULL AND n.recipientUser = :user) OR " +
            "(:company IS NOT NULL AND n.recipientCompany = :company)) AND " +
            "n.isRead = :isRead")
    int countByRecipientAndIsRead(@Param("user") User user,
                                  @Param("company") Company company,
                                  @Param("isRead") boolean isRead);

    /**
     * Đếm số thông báo theo loại (type)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE " +
            "((:user IS NOT NULL AND n.recipientUser = :user) OR " +
            "(:company IS NOT NULL AND n.recipientCompany = :company)) AND " +
            "n.type = :type")
    int countByRecipientAndType(@Param("user") User user,
                                @Param("company") Company company,
                                @Param("type") NotificationType type);
    // Thêm vào NotificationRepository
    @Query("SELECT n FROM Notification n WHERE n.recipientUser = :user AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserAndTypeOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("type") NotificationType type,
            Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.recipientUser = :user AND n.isRead = :isRead AND n.type = :type ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserAndIsReadAndTypeOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("isRead") boolean isRead,
            @Param("type") NotificationType type,
            Pageable pageable);

    @Query("SELECT n.type, COUNT(n) FROM Notification n " +
            "WHERE (n.recipientUser.id = :userId OR n.recipientCompany.id = :companyId) " +  // ✅ OR
            "GROUP BY n.type " +
            "HAVING COUNT(n) > 0 " +
            "ORDER BY n.type")
    List<Object[]> findAvailableTypeCountsByUserOrCompany(
            @Param("userId") Long userId,
            @Param("companyId") Long companyId);

    // ✅ THÊM - All notifications (user OR company)
    @Query("SELECT n FROM Notification n WHERE " +
            "(n.recipientUser = :user OR n.recipientCompany = :company) " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserOrRecipientCompanyOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("company") Company company,
            Pageable pageable);

    // ✅ THÊM - Filter by read status (user OR company)
    @Query("SELECT n FROM Notification n WHERE " +
            "(n.recipientUser = :user OR n.recipientCompany = :company) " +
            "AND n.isRead = :isRead " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserOrRecipientCompanyAndIsReadOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("company") Company company,
            @Param("isRead") boolean isRead,
            Pageable pageable);

    // ✅ THÊM - Filter by type (user OR company)
    @Query("SELECT n FROM Notification n WHERE " +
            "(n.recipientUser = :user OR n.recipientCompany = :company) " +
            "AND n.type = :type " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserOrRecipientCompanyAndTypeOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("company") Company company,
            @Param("type") NotificationType type,
            Pageable pageable);

    // ✅ THÊM - Filter by read status AND type (user OR company)
    @Query("SELECT n FROM Notification n WHERE " +
            "(n.recipientUser = :user OR n.recipientCompany = :company) " +
            "AND n.isRead = :isRead AND n.type = :type " +
            "ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientUserOrRecipientCompanyAndIsReadAndTypeOrderByCreatedAtDesc(
            @Param("user") User user,
            @Param("company") Company company,
            @Param("isRead") boolean isRead,
            @Param("type") NotificationType type,
            Pageable pageable);

    @Query("""
        SELECT 
            n.id AS id,
            n.title AS title,
            n.message AS message,
            n.avatar AS avatar,
            n.href AS href,
            n.type AS type,
            n.isRead AS isRead,
            n.createdAt AS createdAt,
            
            COALESCE(p.fullName, c.name, 'System') AS senderName
            
        FROM Notification n
        LEFT JOIN n.senderUser u
        LEFT JOIN u.personalInfo p
        LEFT JOIN n.senderCompany c
        WHERE 
            (n.recipientUser = :user OR n.recipientCompany = :company)
            AND (:type IS NULL OR n.type = :type)
            AND (:isRead IS NULL OR n.isRead = :isRead)
        ORDER BY n.createdAt DESC
    """)
    Page<NotificationProjection> findByFilters(
            @Param("user") User user,
            @Param("company") Company company,
            @Param("type") NotificationType type,
            @Param("isRead") Boolean isRead,
            Pageable pageable
    );

    @Query("""
                SELECT 
                    COUNT(n) AS allCount,
                    SUM(CASE WHEN n.isRead = false THEN 1 ELSE 0 END) AS unreadCount,
                    SUM(CASE WHEN n.isRead = true THEN 1 ELSE 0 END) AS readCount
                FROM Notification n
                WHERE (n.recipientUser = :user OR n.recipientCompany = :company)
            """)
    NotificationCountProjection countNotifications(
            @Param("user") User user,
            @Param("company") Company company
    );



}