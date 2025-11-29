package com.example.baoNgoCv.model.dto.payOs;


import com.fasterxml.jackson.annotation.JsonProperty;

public record PayOSWebhookDataDto(
        @JsonProperty("orderCode") Long orderCode,
        @JsonProperty("amount") Integer amount,
        @JsonProperty("description") String description,
        @JsonProperty("accountNumber") String accountNumber,
        @JsonProperty("reference") String reference,
        @JsonProperty("transactionDateTime") String transactionDateTime,
        @JsonProperty("currency") String currency,
        @JsonProperty("paymentLinkId") String paymentLinkId,
        @JsonProperty("code") String code, // Mã trạng thái bên trong data
        @JsonProperty("desc") String desc, // Mô tả trạng thái bên trong data
        @JsonProperty("counterAccountBankId") String counterAccountBankId,
        @JsonProperty("counterAccountBankName") String counterAccountBankName,
        @JsonProperty("counterAccountName") String counterAccountName,
        @JsonProperty("counterAccountNumber") String counterAccountNumber,
        @JsonProperty("virtualAccountName") String virtualAccountName,
        @JsonProperty("virtualAccountNumber") String virtualAccountNumber
) {}