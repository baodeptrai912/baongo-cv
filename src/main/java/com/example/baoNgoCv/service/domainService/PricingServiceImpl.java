package com.example.baoNgoCv.service.domainService;

import com.example.baoNgoCv.model.enums.AccountTier;
import com.example.baoNgoCv.model.enums.BillingCycle;
import org.springframework.stereotype.Service;

@Service
public class PricingServiceImpl implements PricingService {

    private static final double YEARLY_DISCOUNT_RATE = 0.85; // Giảm giá 15%

    @Override
    public int getPriceForPlan(AccountTier targetTier, BillingCycle billingCycle) {
        //1. Lấy giá cơ bản trực tiếp từ thuộc tính của enum AccountTier.
        int basePrice = targetTier.getPrice();

        //2. Nếu chu kỳ thanh toán là hàng năm, áp dụng chiết khấu.
        if (billingCycle == BillingCycle.YEARLY) {
            return (int) (basePrice * 12 * YEARLY_DISCOUNT_RATE);
        }

        //3. Nếu không, trả về giá cơ bản hàng tháng.
        return basePrice;
    }
}
