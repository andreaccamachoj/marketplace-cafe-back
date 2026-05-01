package co.com.marketplace.model.orders.gateways;

import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderGateway {
    Mono<Order> save(Order order);
    Mono<Order> findById(UUID id);
    Mono<Order> findByCode(String code);
    Mono<Order> updateStatus(UUID id, OrderStatus status);
    Flux<Order> findByBuyerId(UUID buyerId, OrderStatus statusFilter, int page, int size);
    Mono<Long> countByBuyerId(UUID buyerId, OrderStatus statusFilter);
    Flux<Order> findByProducerId(UUID producerId, OrderStatus statusFilter, int page, int size);
    Mono<Long> countByProducerId(UUID producerId, OrderStatus statusFilter);
    Mono<Integer> nextYearlySequence(int year);
}
