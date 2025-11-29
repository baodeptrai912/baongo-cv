package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.entity.LoginActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginActivityRepository extends JpaRepository<LoginActivity, Long> {

    /**
     * Tìm kiếm lịch sử đăng nhập theo ID của User, có phân trang và sắp xếp.
     * Tên phương thức này sẽ được Spring Data JPA tự động chuyển thành câu lệnh truy vấn.
     */
    Page<LoginActivity> findByUserId(Long userId, Pageable pageable);

    /**
     * Tìm kiếm lịch sử đăng nhập theo ID của Company, có phân trang và sắp xếp.
     */
    Page<LoginActivity> findByCompanyId(Long companyId, Pageable pageable);
}