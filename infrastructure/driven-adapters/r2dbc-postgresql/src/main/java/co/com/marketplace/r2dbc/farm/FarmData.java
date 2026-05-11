package co.com.marketplace.r2dbc.farm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "farms")
public class FarmData {
    @Id
    private UUID id;
    private UUID producerId;
    private String name;
    private String municipality;
    private String department;
    private BigDecimal altitudeMasl;
    private BigDecimal areaHectares;
    private String mainVariety;
    private String process;
    private Integer treeCount;
    private String harvestSeason;
    private BigDecimal annualProductionSacos;
    private BigDecimal yieldPerHa;
    private BigDecimal cuppingScore;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
