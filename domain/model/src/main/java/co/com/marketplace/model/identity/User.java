package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class User {
    UUID id;
    String email;
    String hashedPassword;
    String fullName;
    String phone;
    UserStatus status;
    boolean privacyConsent;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    String role;
}
