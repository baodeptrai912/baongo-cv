package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.jpa.repository.PaymentRepository;
import com.example.baoNgoCv.model.dto.payment.GetTransactionHistoryResponse;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final UserService userService;
    private final PaymentRepository paymentRepository;

    @Override
    public GetTransactionHistoryResponse getTransactionHistory(Pageable pageable) {
        Company currentCompany = userService.getCurrentCompany();
        if (currentCompany == null) {
            log.warn("Attempted to fetch transaction history without an authenticated company.");
            return new GetTransactionHistoryResponse(Page.empty(), 0, 0, 0);
        }

        Long companyId = currentCompany.getId();

        // Lấy trang giao dịch hiện tại
        Page<GetTransactionHistoryResponse.TransactionHistoryProjection> transactions =
                paymentRepository.findByCompanyId(companyId, pageable);

        // Lấy số liệu thống kê cho TẤT CẢ giao dịch của công ty (không phân trang)
        long successfulCount = paymentRepository.countByCompanyIdAndStatus(companyId, PaymentStatus.SUCCESSFUL);
        long pendingCount = paymentRepository.countByCompanyIdAndStatus(companyId, PaymentStatus.PENDING);
        long failedCount = paymentRepository.countByCompanyIdAndStatusIn(companyId,
                java.util.Set.of(PaymentStatus.FAILED));

        return new GetTransactionHistoryResponse(transactions, successfulCount, pendingCount, failedCount);
    }
}
