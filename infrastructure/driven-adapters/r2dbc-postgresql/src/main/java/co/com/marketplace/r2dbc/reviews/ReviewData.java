package co.com.marketplace.r2dbc.reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.ReviewStatusType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "reviews")
public class ReviewData {
    @Id
    private UUID id;
    @Column("product_id")
    private UUID productId;
    @Column("buyer_id")
    private UUID buyerId;
    @Column("order_id")
    private UUID orderId;
    private short rating;
    private String title;
    private String body;
    private ReviewStatusType status;
    @Column("is_verified_purchase")
    private boolean isVerifiedPurchase;
    @Column("helpful_count")
    private int helpfulCount;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
