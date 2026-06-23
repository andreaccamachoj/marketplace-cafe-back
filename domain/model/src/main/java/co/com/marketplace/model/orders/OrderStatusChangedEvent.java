package co.com.marketplace.model.orders;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record OrderStatusChangedEvent(
        UUID orderId,
        String orderCode,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String buyerEmail,
        UUID buyerId,
        BigDecimal totalAmount,
        String note,
        OffsetDateTime changedAt
) {}
