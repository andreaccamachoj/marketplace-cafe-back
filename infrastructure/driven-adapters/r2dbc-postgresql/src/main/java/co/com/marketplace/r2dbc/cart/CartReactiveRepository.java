package co.com.marketplace.r2dbc.cart;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartReactiveRepository extends ReactiveCrudRepository<CartData, UUID> {
    @Query("SELECT * FROM marketplace.carts WHERE user_id = :userId")
    Mono<CartData> findByUserId(UUID userId);
}
