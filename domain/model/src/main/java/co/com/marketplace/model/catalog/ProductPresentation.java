package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class ProductPresentation {
    UUID id;
    UUID productId;
    String presentation;
    BigDecimal extraPrice;
}
