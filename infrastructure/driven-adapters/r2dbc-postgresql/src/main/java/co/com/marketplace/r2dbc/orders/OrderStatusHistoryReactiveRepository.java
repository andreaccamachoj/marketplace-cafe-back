package co.com.marketplace.r2dbc.orders;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface OrderStatusHistoryReactiveRepository extends ReactiveCrudRepository<OrderStatusHistoryData, UUID> {
    @Query("SELECT * FROM marketplace.order_status_history WHERE order_id = :orderId ORDER BY changed_at ASC")
    Flux<OrderStatusHistoryData> findByOrderId(UUID orderId);
}
