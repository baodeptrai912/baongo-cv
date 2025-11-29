package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.exception.companyException.UpgradePlanException;
import com.example.baoNgoCv.model.dto.payOs.PaymentRequest;
import com.example.baoNgoCv.model.entity.Company;
import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.service.securityService.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

@Service
@RequiredArgsConstructor
public class PlanUpgradeServiceImpl implements PlanUpgradeService {

    private final UserService userService;
    private final PaymentService paymentService;
    private final PricingService pricingService;
    private final PayOS payOS;

    @Override
    public CreatePaymentLinkResponse initiatePlanUpgrade(PaymentRequest request) throws PayOSException, UpgradePlanException {
        //1. Lấy thông tin công ty đang đăng nhập từ UserService.
        Company currentUser = userService.getCurrentCompany();
        //2. Xác định gói tài khoản hiện tại và gói tài khoản mục tiêu từ request.
        AccountTier currentTier = currentUser.getSubscriptionDetails().getAccountTier();
        AccountTier targetTier = request.targetTier();

        //3. Kiểm tra logic nghiệp vụ: không cho phép nâng cấp xuống gói thấp hơn hoặc giữ nguyên gói hiện tại.
        if (targetTier.ordinal() <= currentTier.ordinal()) {
            throw new UpgradePlanException("Cannot upgrade to a lower or same tier.");
        }

        //4. Gọi PricingService để quyết định giá chính xác cho gói mục tiêu và chu kỳ thanh toán. Đây là "nguồn chân lý" về giá.
        int expectedAmount = pricingService.getPriceForPlan(targetTier, request.billingCycle());

        //5. Tạo mã đơn hàng (orderCode) và mô tả cho thanh toán.
        long orderCode = System.currentTimeMillis() / 1000;
        String description = String.format("Upgrade %s plan", targetTier.name(), request.billingCycle().name());

        //6. Xây dựng đối tượng CreatePaymentLinkRequest để gửi đến cổng thanh toán PayOS.
        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode)
                .amount((long) expectedAmount) // Sử dụng giá do server quyết định, không tin tưởng client.
                .description(description)
                .returnUrl("https://your-domain.com/payment/success")
                .cancelUrl("https://your-domain.com/payment/cancel")
                .build();

        //7. Gọi API của PayOS để tạo link thanh toán.
        CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

        //8. Lưu lại thông tin đơn hàng vào cơ sở dữ liệu, bao gồm cả billingCycle và qrCode để xử lý ở webhook hoặc quét lại sau.
        paymentService.saveOrder(
                response.getPaymentLinkId(),
                currentUser.getId(),
                orderCode,
                expectedAmount,
                request.billingCycle(),
                targetTier,
                response.getQrCode() // <-- TRUYỀN QR CODE VÀO ĐÂY
        );

        //9. Trả về response chứa link thanh toán cho client.
        return response;
    }
}
