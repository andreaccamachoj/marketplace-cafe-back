package co.com.marketplace.r2dbc.catalog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "roast_levels")
public class RoastLevelData {
    @Id
    private Integer id;
    private String code;
    private String name;
    private String description;
    private String icon;
}
