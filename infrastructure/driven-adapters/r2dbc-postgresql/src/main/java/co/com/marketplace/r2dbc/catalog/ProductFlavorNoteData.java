package co.com.marketplace.r2dbc.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "product_flavor_notes")
public class ProductFlavorNoteData {
    @Id
    private UUID id;
    private UUID productId;
    private String name;
    private String icon;
    private short intensity;
}
