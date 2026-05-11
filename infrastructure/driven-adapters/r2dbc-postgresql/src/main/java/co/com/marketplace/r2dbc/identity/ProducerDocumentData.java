package co.com.marketplace.r2dbc.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.DocStatusType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "producer_documents")
public class ProducerDocumentData {
    @Id
    private UUID id;
    @Column("producer_id")
    private UUID producerId;
    @Column("document_type")
    private String documentType;
    @Column("file_name")
    private String fileName;
    @Column("file_url")
    private String fileUrl;
    private DocStatusType status;
    @Column("uploaded_at")
    private OffsetDateTime uploadedAt;
}
