package co.com.marketplace.model.favorites;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class Favorite {
    UUID id;
    UUID userId;
    UUID productId;
    OffsetDateTime addedAt;
}
