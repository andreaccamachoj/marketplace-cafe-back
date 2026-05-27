package co.com.marketplace.model.cart;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class CartItem {
    UUID id;
    UUID cartId;
    UUID productId;
    int quantity;
    BigDecimal unitPriceSnapshot;
    OffsetDateTime addedAt;
}
