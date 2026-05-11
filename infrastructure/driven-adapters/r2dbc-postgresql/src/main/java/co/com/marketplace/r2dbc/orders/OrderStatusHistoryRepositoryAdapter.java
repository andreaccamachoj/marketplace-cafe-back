package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import co.com.marketplace.r2dbc.type.OrderStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusHistoryRepositoryAdapter implements OrderStatusHistoryGateway {

    private final OrderStatusHistoryReactiveRepository repo;

    @Override
    public Mono<OrderStatusHistory> save(OrderStatusHistory entry) {
        return repo.save(toData(entry))
                .doOnSubscribe(s -> log.debug("[OrderStatusHistoryRepositoryAdapter#save] DB request: orderId={}", entry.getOrderId()))
                .doOnSuccess(r -> log.debug("[OrderStatusHistoryRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[OrderStatusHistoryRepositoryAdapter#save] DB error: {}", e.getMessage()))
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

    private OrderStatusHistoryData toData(OrderStatusHistory h) {
        return OrderStatusHistoryData.builder()
                .id(h.getId())
                .orderId(h.getOrderId())
                .status(OrderStatusType.valueOf(h.getStatus().name()))
                .changedBy(h.getChangedBy())
                .notes(h.getNotes())
                .changedAt(h.getChangedAt())
                .build();
    }
}
