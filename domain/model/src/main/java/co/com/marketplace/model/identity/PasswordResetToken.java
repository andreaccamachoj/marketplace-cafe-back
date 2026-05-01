package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class PasswordResetToken {
    UUID id;
    UUID userId;
    String tokenHash;
    OffsetDateTime expiresAt;
    OffsetDateTime usedAt;
    OffsetDateTime createdAt;
}
