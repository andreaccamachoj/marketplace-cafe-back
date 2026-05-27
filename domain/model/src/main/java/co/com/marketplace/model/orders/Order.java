package co.com.marketplace.model.orders;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Order {
    UUID id;
    UUID buyerId;
    UUID addressId;
    String shippingOptionId;
    Integer couponId;
    String code;
    int yearlySequence;
    int year;
    BigDecimal subtotal;
    BigDecimal shippingAmount;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    OrderStatus status;
    String shippingAddressSnapshot;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    List<OrderItem> items;
}
