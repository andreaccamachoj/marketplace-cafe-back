package co.com.marketplace.model.identity;

import co.com.marketplace.model.shared.DocStatus;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ProducerDocument {
    UUID id;
    UUID producerId;
    String documentType;
    String fileName;
    String fileUrl;
    DocStatus status;
    OffsetDateTime uploadedAt;
}
