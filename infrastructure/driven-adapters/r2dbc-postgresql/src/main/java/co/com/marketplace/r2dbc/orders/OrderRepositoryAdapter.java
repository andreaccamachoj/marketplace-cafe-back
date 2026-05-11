package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.*;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.r2dbc.type.OrderStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderGateway {

    private final OrderReactiveRepository orderRepo;
    private final OrderItemReactiveRepository itemRepo;
    private final DatabaseClient db;

    @Override
    public Mono<Order> save(Order order) {
        return orderRepo.save(toData(order))
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#save] DB request: buyerId={}", order.getBuyerId()))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .flatMap(saved -> {
                    List<OrderItem> items = order.getItems() != null ? order.getItems() : Collections.emptyList();
                    return Flux.fromIterable(items)
                            .flatMap(item -> itemRepo.save(toItemData(item)))
                            .collectList()
                            .map(savedItems -> {
                                List<OrderItem> domainItems = savedItems.stream().map(this::toItemDomain).toList();
                                return toDomain(saved, domainItems);
                            });
                });
    }

    @Override
    public Mono<Order> findById(UUID id) {
        return orderRepo.findById(id)
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .flatMap(data -> itemRepo.findByOrderId(data.getId())
                        .map(this::toItemDomain)
                        .collectList()
                        .map(items -> toDomain(data, items)));
    }

    @Override
    public Mono<Order> findByCode(String code) {
        return orderRepo.findByCode(code)
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#findByCode] DB request: code={}", code))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#findByCode] DB response: found={}", r != null))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#findByCode] DB error: {}", e.getMessage()))
                .flatMap(data -> itemRepo.findByOrderId(data.getId())
                        .map(this::toItemDomain)
                        .collectList()
                        .map(items -> toDomain(data, items)));
    }

    @Override
    public Mono<Order> updateStatus(UUID id, OrderStatus status) {
        return db.sql("UPDATE marketplace.orders SET status = :status, updated_at = NOW() WHERE id = :id RETURNING *")
                .bind("status", OrderStatusType.valueOf(status.name()))
                .bind("id", id)
                .map((row, meta) -> mapRow(row))
                .one()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#updateStatus] DB request: id={}, status={}", id, status))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#updateStatus] DB response: result={}", r != null))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#updateStatus] DB error: {}", e.getMessage()))
                .map(data -> toDomain(data, Collections.emptyList()));
    }

    @Override
    public Flux<Order> findByBuyerId(UUID buyerId, OrderStatus statusFilter, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM marketplace.orders WHERE buyer_id = :buyerId");
        if (statusFilter != null) sql.append(" AND status = :status");
        sql.append(" ORDER BY created_at DESC LIMIT :size OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString())
                .bind("buyerId", buyerId)
                .bind("size", size)
                .bind("offset", (long) page * size);
        if (statusFilter != null) spec = spec.bind("status", OrderStatusType.valueOf(statusFilter.name()));

        return spec.map((row, meta) -> mapRow(row)).all()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#findByBuyerId] DB request: buyerId={}, page={}, size={}", buyerId, page, size))
                .doOnComplete(() -> log.debug("[OrderRepositoryAdapter#findByBuyerId] DB response: complete"))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#findByBuyerId] DB error: {}", e.getMessage()))
                .map(data -> toDomain(data, Collections.emptyList()));
    }

    @Override
    public Mono<Long> countByBuyerId(UUID buyerId, OrderStatus statusFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM marketplace.orders WHERE buyer_id = :buyerId");
        if (statusFilter != null) sql.append(" AND status = :status");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString()).bind("buyerId", buyerId);
        if (statusFilter != null) spec = spec.bind("status", OrderStatusType.valueOf(statusFilter.name()));

        return spec.map((row, meta) -> row.get(0, Long.class)).one()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#countByBuyerId] DB request: buyerId={}", buyerId))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#countByBuyerId] DB response: result={}", r))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#countByBuyerId] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<Order> findByProducerId(UUID producerId, OrderStatus statusFilter, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT o.* FROM marketplace.orders o " +
                "JOIN marketplace.order_items oi ON oi.order_id = o.id " +
                "JOIN marketplace.products p ON p.id = oi.product_id " +
                "WHERE p.producer_id = :producerId");
        if (statusFilter != null) sql.append(" AND o.status = :status");
        sql.append(" ORDER BY o.created_at DESC LIMIT :size OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString())
                .bind("producerId", producerId)
                .bind("size", size)
                .bind("offset", (long) page * size);
        if (statusFilter != null) spec = spec.bind("status", OrderStatusType.valueOf(statusFilter.name()));

        return spec.map((row, meta) -> mapRow(row)).all()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#findByProducerId] DB request: producerId={}, page={}, size={}", producerId, page, size))
                .doOnComplete(() -> log.debug("[OrderRepositoryAdapter#findByProducerId] DB response: complete"))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(data -> toDomain(data, Collections.emptyList()));
    }

    @Override
    public Mono<Long> countByProducerId(UUID producerId, OrderStatus statusFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT o.id) FROM marketplace.orders o " +
                "JOIN marketplace.order_items oi ON oi.order_id = o.id " +
                "JOIN marketplace.products p ON p.id = oi.product_id " +
                "WHERE p.producer_id = :producerId");
        if (statusFilter != null) sql.append(" AND o.status = :status");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString()).bind("producerId", producerId);
        if (statusFilter != null) spec = spec.bind("status", OrderStatusType.valueOf(statusFilter.name()));

        return spec.map((row, meta) -> row.get(0, Long.class)).one()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#countByProducerId] DB request: producerId={}", producerId))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#countByProducerId] DB response: result={}", r))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#countByProducerId] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Integer> nextYearlySequence(int year) {
        return db.sql("SELECT marketplace.fn_next_order_seq(:year)")
                .bind("year", year)
                .map((row, meta) -> row.get(0, Integer.class))
                .one()
                .doOnSubscribe(s -> log.debug("[OrderRepositoryAdapter#nextYearlySequence] DB request: year={}", year))
                .doOnSuccess(r -> log.debug("[OrderRepositoryAdapter#nextYearlySequence] DB response: result={}", r))
                .doOnError(e -> log.error("[OrderRepositoryAdapter#nextYearlySequence] DB error: {}", e.getMessage()));
    }

    private OrderData mapRow(io.r2dbc.spi.Row row) {
        return OrderData.builder()
                .id(row.get("id", UUID.class))
                .buyerId(row.get("buyer_id", UUID.class))
                .addressId(row.get("address_id", UUID.class))
                .shippingOptionId(row.get("shipping_option_id", String.class))
                .couponId(row.get("coupon_id", Integer.class))
                .code(row.get("code", String.class))
                .yearlySequence(row.get("yearly_sequence", Integer.class))
                .year(row.get("year", Integer.class))
                .subtotal(row.get("subtotal", BigDecimal.class))
                .shippingAmount(row.get("shipping_amount", BigDecimal.class))
                .discountAmount(row.get("discount_amount", BigDecimal.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .status(row.get("status", OrderStatusType.class))
                .shippingAddressSnapshot(row.get("shipping_address_snapshot", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private Order toDomain(OrderData d, List<OrderItem> items) {
        return Order.builder()
                .id(d.getId())
                .buyerId(d.getBuyerId())
                .addressId(d.getAddressId())
                .shippingOptionId(d.getShippingOptionId())
                .couponId(d.getCouponId())
                .code(d.getCode())
                .yearlySequence(d.getYearlySequence())
                .year(d.getYear())
                .subtotal(d.getSubtotal())
                .shippingAmount(d.getShippingAmount())
                .discountAmount(d.getDiscountAmount())
                .totalAmount(d.getTotalAmount())
                .status(OrderStatus.valueOf(d.getStatus().name()))
                .shippingAddressSnapshot(d.getShippingAddressSnapshot())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .items(items)
                .build();
    }

    private OrderData toData(Order o) {
        return OrderData.builder()
                .id(o.getId())
                .buyerId(o.getBuyerId())
                .addressId(o.getAddressId())
                .shippingOptionId(o.getShippingOptionId())
                .couponId(o.getCouponId())
                .code(o.getCode())
                .yearlySequence(o.getYearlySequence())
                .year(o.getYear())
                .subtotal(o.getSubtotal())
                .shippingAmount(o.getShippingAmount())
                .discountAmount(o.getDiscountAmount())
                .totalAmount(o.getTotalAmount())
                .status(OrderStatusType.valueOf(o.getStatus().name()))
                .shippingAddressSnapshot(o.getShippingAddressSnapshot())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }

    private OrderItem toItemDomain(OrderItemData d) {
        return OrderItem.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .productId(d.getProductId())
                .productNameSnapshot(d.getProductNameSnapshot())
                .productEmojiSnapshot(d.getProductEmojiSnapshot())
                .quantity(d.getQuantity())
                .unitPriceSnapshot(d.getUnitPriceSnapshot())
                .subtotal(d.getSubtotal())
                .build();
    }

    private OrderItemData toItemData(OrderItem i) {
        return OrderItemData.builder()
                .id(i.getId())
                .orderId(i.getOrderId())
                .productId(i.getProductId())
                .productNameSnapshot(i.getProductNameSnapshot())
                .productEmojiSnapshot(i.getProductEmojiSnapshot())
                .quantity(i.getQuantity())
                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                .subtotal(i.getSubtotal())
                .build();
    }
}
