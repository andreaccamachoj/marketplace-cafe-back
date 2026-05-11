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
@Table(schema = "marketplace", name = "privacy_consents")
public class PrivacyConsentData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    @Column("policy_version")
    private String policyVersion;
    @Column("accepted_at")
    private OffsetDateTime acceptedAt;
    @Column("ip_address")
    private String ipAddress;
}
