package co.com.marketplace.model.orders;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class OrderItem {
    UUID id;
    UUID orderId;
    UUID productId;
    String productNameSnapshot;
    String productEmojiSnapshot;
    int quantity;
    BigDecimal unitPriceSnapshot;
    BigDecimal subtotal;
}
