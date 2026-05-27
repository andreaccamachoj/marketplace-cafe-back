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
@Table(schema = "marketplace", name = "buyer_profiles")
public class BuyerProfileData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    private String city;
    private String department;
    @Column("preferred_payment")
    private String preferredPayment;
    @Column("newsletter_opt_in")
    private boolean newsletterOptIn;
    @Column("avatar_initials")
    private String avatarInitials;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
