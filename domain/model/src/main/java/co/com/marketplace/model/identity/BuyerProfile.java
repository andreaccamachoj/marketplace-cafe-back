package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class BuyerProfile {
    UUID id;
    UUID userId;
    String city;
    String department;
    String preferredPayment;
    boolean newsletterOptIn;
    String avatarInitials;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
