package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import co.com.marketplace.r2dbc.type.OrderStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusHistoryRepositoryAdapter implements OrderStatusHistoryGateway {

    private final OrderStatusHistoryReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<OrderStatusHistory> save(OrderStatusHistory entry) {
        DatabaseClient.GenericExecuteSpec spec = db.sql(
                "INSERT INTO marketplace.order_status_history " +
                "(order_id, status, changed_by, notes, changed_at) " +
                "VALUES (:orderId, CAST(:status AS marketplace.order_status), :changedBy, :notes, :changedAt) " +
                "RETURNING *")
                .bind("orderId", entry.getOrderId())
                .bind("status", entry.getStatus().name())
                .bind("notes", entry.getNotes() != null ? entry.getNotes() : "");
        spec = entry.getChangedBy() != null
                ? spec.bind("changedBy", entry.getChangedBy())
                : spec.bindNull("changedBy", UUID.class);
        spec = entry.getChangedAt() != null
                ? spec.bind("changedAt", entry.getChangedAt())
                : spec.bind("changedAt", OffsetDateTime.now());

        return spec.map((row, meta) -> OrderStatusHistoryData.builder()
                        .id(row.get("id", UUID.class))
                        .orderId(row.get("order_id", UUID.class))
                        .status(OrderStatusType.valueOf(row.get("status", String.class)))
                        .changedBy(row.get("changed_by", UUID.class))
                        .notes(row.get("notes", String.class))
                        .changedAt(row.get("changed_at", OffsetDateTime.class))
                        .build()).one()
                .doOnSubscribe(s -> log.debug("[OrderStatusHistoryRepositoryAdapter#save] DB request: orderId={}", entry.getOrderId()))
                .doOnSuccess(r -> log.debug("[OrderStatusHistoryRepositoryAdapter#save] DB response: id={}", r != null ? r.getId() : null))
                .doOnError(e -> log.error("[OrderStatusHistoryRepositoryAdapter#save] DB error [{}]: {}", e.getClass().getSimpleName(), e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<OrderStatusHistory> findByOrderId(UUID orderId) {
        return repo.findByOrderId(orderId)
                .doOnSubscribe(s -> log.debug("[OrderStatusHistoryRepositoryAdapter#findByOrderId] DB request: orderId={}", orderId))
                .doOnComplete(() -> log.debug("[OrderStatusHistoryRepositoryAdapter#findByOrderId] DB response: complete"))
                .doOnError(e -> log.error("[OrderStatusHistoryRepositoryAdapter#findByOrderId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private OrderStatusHistory toDomain(OrderStatusHistoryData d) {
        return OrderStatusHistory.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .status(OrderStatus.valueOf(d.getStatus().name()))
                .changedBy(d.getChangedBy())
                .notes(d.getNotes())
                .changedAt(d.getChangedAt())
                .build();
    }
}
