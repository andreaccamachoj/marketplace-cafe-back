package co.com.marketplace.model.favorites.gateways;

import co.com.marketplace.model.favorites.Favorite;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FavoriteGateway {
    Flux<Favorite> findByUserId(UUID userId);
    Mono<Favorite> save(Favorite favorite);
    Mono<Void> delete(UUID userId, UUID productId);
    Mono<Boolean> exists(UUID userId, UUID productId);
}
