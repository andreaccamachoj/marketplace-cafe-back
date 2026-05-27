package co.com.marketplace.r2dbc.orders;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderPaymentReactiveRepository extends ReactiveCrudRepository<OrderPaymentData, UUID> {
    @Query("SELECT * FROM marketplace.order_payments WHERE order_id = :orderId")
    Mono<OrderPaymentData> findByOrderId(UUID orderId);
}
