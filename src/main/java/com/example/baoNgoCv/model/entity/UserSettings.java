package com.example.baoNgoCv.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserSettings {

    /**
     * ✅ IMPROVED: Shared Primary Key.
     * Khoá chính của UserSettings sẽ được chia sẻ trực tiếp từ User.
     * Điều này đảm bảo mối quan hệ 1-1 chặt chẽ.
     */
    @Id
    @Column(name = "user_id")
    private Long id; // Không còn @GeneratedValue

    /**
     * Cài đặt nhận email khi có cập nhật về đơn ứng tuyển.
     */
    @Column(name = "email_on_application_update")
    @ColumnDefault("true")
    private boolean emailOnApplicationUpdate = true;

    /**
     * Cài đặt hiển thị công khai hồ sơ.
     */
    @Column(name = "is_profile_public")
    @ColumnDefault("true")
    private boolean profilePublic = true;

    /**
     * ✅ IMPROVED: @MapsId mapping.
     * Liên kết One-to-One với User.
     * @MapsId chỉ định rằng khoá chính (id) của entity này được "ánh xạ" từ User.
     * @JoinColumn chỉ định rằng cột 'user_id' vừa là khoá chính, vừa là khoá ngoại.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public UserSettings(User user) {
        this.user = user;
        this.id = user.getId(); // Gán ID một cách tường minh khi khởi tạo
    }
}