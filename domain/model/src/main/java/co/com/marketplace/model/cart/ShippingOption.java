package co.com.marketplace.model.cart;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ShippingOption {
    String id;
    String name;
    String deliveryWindow;
    BigDecimal price;
    boolean isActive;
    int displayOrder;
}
