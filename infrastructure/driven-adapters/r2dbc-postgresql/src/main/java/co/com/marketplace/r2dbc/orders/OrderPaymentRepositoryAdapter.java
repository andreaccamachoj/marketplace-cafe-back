package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import co.com.marketplace.r2dbc.type.PaymentStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaymentRepositoryAdapter implements OrderPaymentGateway {

    private final OrderPaymentReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<OrderPayment> save(OrderPayment payment) {
        return repo.save(toData(payment))
                .doOnSubscribe(s -> log.debug("[OrderPaymentRepositoryAdapter#save] DB request: orderId={}", payment.getOrderId()))
                .doOnSuccess(r -> log.debug("[OrderPaymentRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[OrderPaymentRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<OrderPayment> findByOrderId(UUID orderId) {
        return repo.findByOrderId(orderId)
                .doOnSubscribe(s -> log.debug("[OrderPaymentRepositoryAdapter#findByOrderId] DB request: orderId={}", orderId))
                .doOnSuccess(r -> log.debug("[OrderPaymentRepositoryAdapter#findByOrderId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[OrderPaymentRepositoryAdapter#findByOrderId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<OrderPayment> updateStatus(UUID id, PaymentStatus status, UUID verifiedBy) {
        return db.sql("UPDATE marketplace.order_payments SET status = :status, verified_by = :verifiedBy, verified_at = NOW() WHERE id = :id RETURNING *")
                .bind("status", PaymentStatusType.valueOf(status.name()))
                .bind("id", id)
                .bindNull("verifiedBy", UUID.class)
                .bind("verifiedBy", verifiedBy)
                .map((row, meta) -> OrderPaymentData.builder()
                        .id(row.get("id", UUID.class))
                        .orderId(row.get("order_id", UUID.class))
                        .paymentMethodId(row.get("payment_method_id", UUID.class))
                        .paymentMethodCode(row.get("payment_method_code", String.class))
                        .amount(row.get("amount", BigDecimal.class))
                        .status(row.get("status", PaymentStatusType.class))
                        .reference(row.get("reference", String.class))
                        .proofUrl(row.get("proof_url", String.class))
                        .submittedAt(row.get("submitted_at", OffsetDateTime.class))
                        .verifiedAt(row.get("verified_at", OffsetDateTime.class))
                        .verifiedBy(row.get("verified_by", UUID.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[OrderPaymentRepositoryAdapter#updateStatus] DB request: id={}, status={}", id, status))
                .doOnSuccess(r -> log.debug("[OrderPaymentRepositoryAdapter#updateStatus] DB response: result={}", r != null))
                .doOnError(e -> log.error("[OrderPaymentRepositoryAdapter#updateStatus] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private OrderPayment toDomain(OrderPaymentData d) {
        return OrderPayment.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .paymentMethodId(d.getPaymentMethodId())
                .paymentMethodCode(d.getPaymentMethodCode())
                .amount(d.getAmount())
                .status(PaymentStatus.valueOf(d.getStatus().name()))
                .reference(d.getReference())
                .proofUrl(d.getProofUrl())
                .submittedAt(d.getSubmittedAt())
                .verifiedAt(d.getVerifiedAt())
                .verifiedBy(d.getVerifiedBy())
                .build();
    }

    private OrderPaymentData toData(OrderPayment p) {
        return OrderPaymentData.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .paymentMethodId(p.getPaymentMethodId())
                .paymentMethodCode(p.getPaymentMethodCode())
                .amount(p.getAmount())
                .status(PaymentStatusType.valueOf(p.getStatus().name()))
                .reference(p.getReference())
                .proofUrl(p.getProofUrl())
                .submittedAt(p.getSubmittedAt())
                .verifiedAt(p.getVerifiedAt())
                .verifiedBy(p.getVerifiedBy())
                .build();
    }
}
