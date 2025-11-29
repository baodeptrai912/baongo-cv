package com.example.baoNgoCv.jpa.repository;

import com.example.baoNgoCv.model.dto.payment.GetTransactionHistoryResponse;
import com.example.baoNgoCv.model.entity.Payment;
import com.example.baoNgoCv.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p " +
            "JOIN FETCH p.company c " +
            "WHERE p.orderCode = :orderCode")
    Optional<Payment> findByOrderCode(@Param("orderCode") Long orderCode);

    @Query("SELECT p.orderCode as orderCode, p.amount as amount, p.targetTier as targetTier, p.billingCycle as billingCycle, p.status as status, p.createdAt as createdAt, p.qrCode as qrCode " +
           "FROM Payment p WHERE p.company.id = :companyId ORDER BY p.createdAt DESC")
    Page<GetTransactionHistoryResponse.TransactionHistoryProjection> findByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    long countByCompanyIdAndStatus(Long companyId, PaymentStatus status);

    long countByCompanyIdAndStatusIn(Long companyId, Set<PaymentStatus> statuses);

    long countByCompanyId(Long companyId);

    @Modifying
    @Transactional
    @Query("UPDATE Payment p SET p.status = :failedStatus, p.qrCode = null " +
           "WHERE p.company.id = :companyId " +
           "AND p.status = :pendingStatus " +
           "AND p.orderCode <> :successfulOrderCode")
    void failOtherPendingPayments(@Param("companyId") Long companyId,
                                  @Param("successfulOrderCode") Long successfulOrderCode,
                                  @Param("pendingStatus") PaymentStatus pendingStatus,
                                  @Param("failedStatus") PaymentStatus failedStatus);
}
