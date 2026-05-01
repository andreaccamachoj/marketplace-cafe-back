package co.com.marketplace.model.farm;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Farm {
    UUID id;
    UUID producerId;
    String name;
    String municipality;
    String department;
    BigDecimal altitudeMasl;
    BigDecimal areaHectares;
    String mainVariety;
    String process;
    Integer treeCount;
    String harvestSeason;
    BigDecimal annualProductionSacos;
    BigDecimal yieldPerHa;
    BigDecimal cuppingScore;
    String description;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
