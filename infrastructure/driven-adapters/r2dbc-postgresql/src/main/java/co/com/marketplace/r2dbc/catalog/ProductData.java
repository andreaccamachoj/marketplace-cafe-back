package co.com.marketplace.r2dbc.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "products")
public class ProductData {
    @Id
    private UUID id;
    private UUID producerId;
    private UUID categoryId;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private String unit;
    private String region;
    private String emoji;
    private String coverImageUrl;
    private int soldCount;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    @Transient private String producerName;
    @Transient private String categoryName;
    @Transient private double rating;
    @Transient private int reviewCount;
    @Transient private int stock;
    @Transient private String certCodes;
}
