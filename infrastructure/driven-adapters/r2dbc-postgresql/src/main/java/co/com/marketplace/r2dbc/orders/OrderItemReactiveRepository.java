package co.com.marketplace.r2dbc.orders;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderItemReactiveRepository extends ReactiveCrudRepository<OrderItemData, UUID> {
    @Query("SELECT * FROM marketplace.order_items WHERE order_id = :orderId")
    Flux<OrderItemData> findByOrderId(UUID orderId);
}
