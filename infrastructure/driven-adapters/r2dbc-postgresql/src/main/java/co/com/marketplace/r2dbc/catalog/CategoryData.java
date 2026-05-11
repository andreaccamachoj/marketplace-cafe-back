package co.com.marketplace.r2dbc.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "categories")
public class CategoryData {
    @Id
    private UUID id;
    private String name;
    private String slug;
    private String description;
    private UUID parentId;
    private boolean isActive;
    private String iconEmoji;
    private OffsetDateTime createdAt;
}
