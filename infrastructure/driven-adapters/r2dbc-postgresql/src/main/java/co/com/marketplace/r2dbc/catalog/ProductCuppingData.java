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
@Table(schema = "marketplace", name = "product_cupping")
public class ProductCuppingData {
    @Id
    private UUID productId;
    private BigDecimal score;
    private Short aroma;
    private Short flavor;
    private Short body;
    private Short finish;
    private Short acidity;
}
