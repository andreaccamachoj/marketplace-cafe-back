package co.com.marketplace.r2dbc.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.OrderStatusType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "orders")
public class OrderData {
    @Id
    private UUID id;
    @Column("buyer_id")
    private UUID buyerId;
    @Column("address_id")
    private UUID addressId;
    @Column("shipping_option_id")
    private String shippingOptionId;
    @Column("coupon_id")
    private Integer couponId;
    private String code;
    @Column("yearly_sequence")
    private int yearlySequence;
    private int year;
    private BigDecimal subtotal;
    @Column("shipping_amount")
    private BigDecimal shippingAmount;
    @Column("discount_amount")
    private BigDecimal discountAmount;
    @Column("total_amount")
    private BigDecimal totalAmount;
    private OrderStatusType status;
    @Column("shipping_address_snapshot")
    private String shippingAddressSnapshot;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
