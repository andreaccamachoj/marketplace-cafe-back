package co.com.marketplace.r2dbc.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.UserStatusType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "users")
public class UserData {
    @Id
    private UUID id;
    private String email;
    @Column("password_hash")
    private String passwordHash;
    @Column("full_name")
    private String fullName;
    private String phone;
    private UserStatusType status;
    @Column("privacy_consent")
    private boolean privacyConsent;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
