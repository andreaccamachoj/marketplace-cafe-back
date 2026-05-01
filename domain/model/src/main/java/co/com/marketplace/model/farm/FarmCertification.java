package co.com.marketplace.model.farm;

import co.com.marketplace.model.shared.DocStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class FarmCertification {
    UUID id;
    UUID farmId;
    Integer certificationId;
    String issuer;
    LocalDate issueDate;
    LocalDate expiryDate;
    DocStatus status;
    String documentUrl;
    String notes;
}
