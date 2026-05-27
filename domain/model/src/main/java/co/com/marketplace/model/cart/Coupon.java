package co.com.marketplace.model.cart;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Value
@Builder
public class Coupon {
    Integer id;
    String code;
    String description;
    CouponDiscountType discountType;
    BigDecimal discountValue;
    BigDecimal minSubtotal;
    Integer usageLimit;
    int usedCount;
    OffsetDateTime validFrom;
    OffsetDateTime validUntil;
    boolean isActive;
}
