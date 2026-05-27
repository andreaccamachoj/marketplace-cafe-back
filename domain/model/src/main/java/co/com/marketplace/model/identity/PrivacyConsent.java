package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class PrivacyConsent {
    UUID id;
    UUID userId;
    String policyVersion;
    OffsetDateTime acceptedAt;
    String ipAddress;
}
