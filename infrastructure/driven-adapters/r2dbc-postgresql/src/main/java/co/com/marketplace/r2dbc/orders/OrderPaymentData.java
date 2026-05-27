package co.com.marketplace.r2dbc.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.PaymentStatusType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "order_payments")
public class OrderPaymentData {
    @Id
    private UUID id;
    @Column("order_id")
    private UUID orderId;
    @Column("payment_method_id")
    private UUID paymentMethodId;
    @Column("payment_method_code")
    private String paymentMethodCode;
    private BigDecimal amount;
    private PaymentStatusType status;
    private String reference;
    @Column("proof_url")
    private String proofUrl;
    @Column("submitted_at")
    private OffsetDateTime submittedAt;
    @Column("verified_at")
    private OffsetDateTime verifiedAt;
    @Column("verified_by")
    private UUID verifiedBy;
}
