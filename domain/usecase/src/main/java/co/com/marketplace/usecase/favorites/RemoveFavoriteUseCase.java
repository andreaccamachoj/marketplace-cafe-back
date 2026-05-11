package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class RemoveFavoriteUseCase {

    private final FavoriteGateway favoriteGateway;

    public RemoveFavoriteUseCase(FavoriteGateway favoriteGateway) {
        this.favoriteGateway = favoriteGateway;
    }

    public Mono<Void> execute(UUID userId, UUID productId) {
        return favoriteGateway.delete(userId, productId);
    }
}
