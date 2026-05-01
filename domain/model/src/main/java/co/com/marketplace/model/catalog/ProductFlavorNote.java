package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ProductFlavorNote {
    UUID id;
    UUID productId;
    String name;
    String icon;
    short intensity;
}
