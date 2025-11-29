package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.dto.payment.GetTransactionHistoryResponse;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    GetTransactionHistoryResponse getTransactionHistory(Pageable pageable);
}
