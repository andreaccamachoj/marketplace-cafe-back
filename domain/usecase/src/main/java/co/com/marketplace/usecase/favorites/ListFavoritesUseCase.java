package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListFavoritesUseCase {

    private final FavoriteGateway favoriteGateway;

    public ListFavoritesUseCase(FavoriteGateway favoriteGateway) {
        this.favoriteGateway = favoriteGateway;
    }

    public Flux<Favorite> execute(UUID userId) {
        return favoriteGateway.findByUserId(userId);
    }
}
