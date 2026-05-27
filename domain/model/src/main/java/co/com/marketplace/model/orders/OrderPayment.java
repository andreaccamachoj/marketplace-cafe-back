package co.com.marketplace.model.orders;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class OrderPayment {
    UUID id;
    UUID orderId;
    UUID paymentMethodId;
    String paymentMethodCode;
    BigDecimal amount;
    PaymentStatus status;
    String reference;
    String proofUrl;
    OffsetDateTime submittedAt;
    OffsetDateTime verifiedAt;
    UUID verifiedBy;
}
