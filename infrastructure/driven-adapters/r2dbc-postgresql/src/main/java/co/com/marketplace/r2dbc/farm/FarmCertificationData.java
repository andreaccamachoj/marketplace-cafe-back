package co.com.marketplace.r2dbc.farm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.DocStatusType;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "farm_certifications")
public class FarmCertificationData {
    @Id
    private UUID id;
    private UUID farmId;
    private Integer certificationId;
    private String issuer;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private DocStatusType status;
    private String documentUrl;
    private String notes;
}
