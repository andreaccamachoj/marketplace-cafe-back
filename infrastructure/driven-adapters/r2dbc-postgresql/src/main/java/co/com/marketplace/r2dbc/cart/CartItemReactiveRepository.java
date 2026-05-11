package co.com.marketplace.r2dbc.cart;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartItemReactiveRepository extends ReactiveCrudRepository<CartItemData, UUID> {
    @Query("SELECT * FROM marketplace.cart_items WHERE cart_id = :cartId")
    Flux<CartItemData> findByCartId(UUID cartId);

    @Query("DELETE FROM marketplace.cart_items WHERE cart_id = :cartId")
    Mono<Void> deleteByCartId(UUID cartId);
}
