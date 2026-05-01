package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Address {
    UUID id;
    UUID userId;
    String label;
    String line1;
    String line2;
    String city;
    String department;
    String zipCode;
    boolean isDefault;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
