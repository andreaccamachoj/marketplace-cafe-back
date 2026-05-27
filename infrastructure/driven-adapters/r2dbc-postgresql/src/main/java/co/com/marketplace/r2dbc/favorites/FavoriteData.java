package co.com.marketplace.r2dbc.favorites;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "favorites")
public class FavoriteData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    @Column("product_id")
    private UUID productId;
    @Column("added_at")
    private OffsetDateTime addedAt;
}
