package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Product {
    UUID id;
    UUID producerId;
    String producerName;
    UUID categoryId;
    String categoryName;
    String name;
    String description;
    BigDecimal price;
    BigDecimal originalPrice;
    BigDecimal discountPercent;
    String unit;
    String region;
    String emoji;
    int soldCount;
    double rating;
    int reviewCount;
    int stock;
    ProductStatus status;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    List<ProductImage> images;
    List<ProductPresentation> presentations;
    List<ProductFlavorNote> flavorNotes;
    List<Integer> roastLevelIds;
    List<String> certificationCodes;
    ProductCupping cupping;
}
