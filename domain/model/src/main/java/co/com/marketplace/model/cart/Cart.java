package co.com.marketplace.model.cart;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Cart {
    UUID id;
    UUID userId;
    Integer couponId;
    String shippingOptionId;
    List<CartItem> items;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
