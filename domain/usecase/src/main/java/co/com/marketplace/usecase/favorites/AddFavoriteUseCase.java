package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class AddFavoriteUseCase {

    private final FavoriteGateway favoriteGateway;

    public Mono<Favorite> execute(UUID userId, UUID productId) {
        return favoriteGateway.exists(userId, productId)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("FAVORITE_ALREADY_EXISTS", "Product already in favorites"));
                    }
                    Favorite favorite = Favorite.builder()
                            .id(UUID.randomUUID())
                            .userId(userId)
                            .productId(productId)
                            .addedAt(OffsetDateTime.now())
                            .build();
                    return favoriteGateway.save(favorite);
                });
    }
}
