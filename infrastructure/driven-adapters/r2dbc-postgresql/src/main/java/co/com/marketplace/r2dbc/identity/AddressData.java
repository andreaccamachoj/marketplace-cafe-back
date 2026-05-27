package co.com.marketplace.r2dbc.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "addresses")
public class AddressData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    private String label;
    private String line1;
    private String line2;
    private String city;
    private String department;
    @Column("zip_code")
    private String zipCode;
    @Column("is_default")
    private boolean isDefault;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
