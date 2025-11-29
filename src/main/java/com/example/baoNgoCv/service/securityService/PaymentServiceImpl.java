package com.example.baoNgoCv.service.securityService;

import com.example.baoNgoCv.event.company.UpgradePlanSuccessEvent;
import com.example.baoNgoCv.exception.companyException.CompanyNotFoundException;
import com.example.baoNgoCv.exception.companyException.PaymentNotFoundException;
import com.example.baoNgoCv.jpa.repository.CompanyRepository;
import com.example.baoNgoCv.jpa.repository.PaymentRepository;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.entity.Payment;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import com.example.baoNgoCv.model.enums.PaymentStatus;
import com.example.baoNgoCv.model.valueObject.SubscriptionDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CompanyRepository companyRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void saveOrder(String paymentLinkId, Long companyId, Long orderCode, Integer amount, BillingCycle billingCycle, AccountTier targetTier, String qrCode) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException());

        Payment payment = Payment.create(paymentLinkId, company, orderCode, amount, billingCycle, targetTier, qrCode);

        paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void handleSuccessfulPayment(Long orderCode) {
        //1. Tìm kiếm giao dịch thanh toán trong DB bằng orderCode.
        Payment payment = paymentRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order code: " + orderCode));

        //2. Cập nhật trạng thái của giao dịch thành SUCCESSFUL.
        payment.updateStatusSuccess();

        //3. Lấy thông tin công ty liên quan đến thanh toán này.
        Company company = payment.getCompany();
        if (company == null) {
            throw new CompanyNotFoundException();
        }

        //4. Hủy tất cả các giao dịch PENDING khác của cùng công ty.
        paymentRepository.failOtherPendingPayments(
                company.getId(),
                orderCode,
                PaymentStatus.PENDING,
                PaymentStatus.FAILED
        );

        //5. Lấy lại thông tin cần thiết để nâng cấp.
        BillingCycle cycle = payment.getBillingCycle();
        AccountTier targetTier = payment.getTargetTier();
        if (cycle == null || targetTier == null) {
            throw new IllegalStateException("Payment is missing billing cycle or target tier information for order code: " + orderCode);
        }

        //6. Ủy quyền cho đối tượng Company tự thực hiện việc nâng cấp.
        company.upgradeSubscription(targetTier, cycle);

        //7. Tạo sự kiện để thông báo cho các phần khác của hệ thống.
        UpgradePlanSuccessEvent event = new UpgradePlanSuccessEvent(
                company.getId(),
                company.getUsername(),
                company.getContactEmail(),
                company.getName(),
                company.getSubscriptionDetails().getAccountTier()
        );

        //8. Xuất bản sự kiện.
        applicationEventPublisher.publishEvent(event);
    }
}
