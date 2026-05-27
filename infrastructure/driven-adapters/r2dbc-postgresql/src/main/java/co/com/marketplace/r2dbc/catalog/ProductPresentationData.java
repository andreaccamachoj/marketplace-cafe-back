package co.com.marketplace.r2dbc.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "product_presentations")
public class ProductPresentationData {
    @Id
    private UUID id;
    private UUID productId;
    private String presentation;
    private BigDecimal extraPrice;
}
