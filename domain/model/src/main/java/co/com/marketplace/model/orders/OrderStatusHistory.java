package co.com.marketplace.model.orders;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class OrderStatusHistory {
    UUID id;
    UUID orderId;
    OrderStatus status;
    UUID changedBy;
    String notes;
    OffsetDateTime changedAt;
}
