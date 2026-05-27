package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ProductImage {
    UUID id;
    UUID productId;
    String imageUrl;
    int displayOrder;
    OffsetDateTime uploadedAt;
}
