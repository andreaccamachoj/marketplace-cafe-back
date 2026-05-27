package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Category {
    UUID id;
    String name;
    String slug;
    String description;
    UUID parentId;
    boolean isActive;
    String iconEmoji;
    OffsetDateTime createdAt;
}
