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
@Table(schema = "marketplace", name = "password_reset_tokens")
public class PasswordResetTokenData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    @Column("token_hash")
    private String tokenHash;
    @Column("expires_at")
    private OffsetDateTime expiresAt;
    @Column("used_at")
    private OffsetDateTime usedAt;
    @Column("created_at")
    private OffsetDateTime createdAt;
}
