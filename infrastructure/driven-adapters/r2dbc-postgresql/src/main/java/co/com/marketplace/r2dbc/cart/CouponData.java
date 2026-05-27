package co.com.marketplace.r2dbc.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.CouponDiscountType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "coupons")
public class CouponData {
    @Id
    private Integer id;
    private String code;
    private String description;
    @Column("discount_type")
    private CouponDiscountType discountType;
    @Column("discount_value")
    private BigDecimal discountValue;
    @Column("min_subtotal")
    private BigDecimal minSubtotal;
    @Column("usage_limit")
    private Integer usageLimit;
    @Column("used_count")
    private int usedCount;
    @Column("valid_from")
    private OffsetDateTime validFrom;
    @Column("valid_until")
    private OffsetDateTime validUntil;
    @Column("is_active")
    private boolean isActive;
}
