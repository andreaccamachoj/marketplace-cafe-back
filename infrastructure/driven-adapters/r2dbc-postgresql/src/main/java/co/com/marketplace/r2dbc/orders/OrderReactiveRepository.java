package co.com.marketplace.r2dbc.orders;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderReactiveRepository extends ReactiveCrudRepository<OrderData, UUID> {
    @Query("SELECT * FROM marketplace.orders WHERE code = :code")
    Mono<OrderData> findByCode(String code);
}
