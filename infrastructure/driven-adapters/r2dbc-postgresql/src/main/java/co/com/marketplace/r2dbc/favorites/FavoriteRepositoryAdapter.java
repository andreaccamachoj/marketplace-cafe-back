package co.com.marketplace.r2dbc.favorites;

import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FavoriteRepositoryAdapter implements FavoriteGateway {

    private final FavoriteReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Flux<Favorite> findByUserId(UUID userId) {
        return repo.findByUserId(userId)
                .doOnSubscribe(s -> log.debug("[FavoriteRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnComplete(() -> log.debug("[FavoriteRepositoryAdapter#findByUserId] DB response: complete"))
                .doOnError(e -> log.error("[FavoriteRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Favorite> save(Favorite favorite) {
        return db.sql("INSERT INTO marketplace.favorites (id, user_id, product_id, added_at) " +
                      "VALUES (:id, :userId, :productId, :addedAt)")
                .bind("id", favorite.getId())
                .bind("userId", favorite.getUserId())
                .bind("productId", favorite.getProductId())
                .bind("addedAt", favorite.getAddedAt())
                .then()
                .thenReturn(favorite)
                .doOnSubscribe(s -> log.debug("[FavoriteRepositoryAdapter#save] DB request: userId={}, productId={}", favorite.getUserId(), favorite.getProductId()))
                .doOnSuccess(r -> log.debug("[FavoriteRepositoryAdapter#save] DB response: saved"))
                .doOnError(e -> log.error("[FavoriteRepositoryAdapter#save] DB error: {} {}", e.getClass().getSimpleName(), e.getMessage()));
    }

    @Override
    public Mono<Void> delete(UUID userId, UUID productId) {
        return db.sql("DELETE FROM marketplace.favorites WHERE user_id = :userId AND product_id = :productId")
                .bind("userId", userId)
                .bind("productId", productId)
                .then()
                .doOnSubscribe(s -> log.debug("[FavoriteRepositoryAdapter#delete] DB request: userId={}, productId={}", userId, productId))
                .doOnTerminate(() -> log.debug("[FavoriteRepositoryAdapter#delete] DB response: done"))
                .doOnError(e -> log.error("[FavoriteRepositoryAdapter#delete] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> exists(UUID userId, UUID productId) {
        return db.sql("SELECT COUNT(*) > 0 FROM marketplace.favorites WHERE user_id = :userId AND product_id = :productId")
                .bind("userId", userId)
                .bind("productId", productId)
                .map((row, meta) -> row.get(0, Boolean.class))
                .one()
                .doOnSubscribe(s -> log.debug("[FavoriteRepositoryAdapter#exists] DB request: userId={}, productId={}", userId, productId))
                .doOnSuccess(r -> log.debug("[FavoriteRepositoryAdapter#exists] DB response: result={}", r))
                .doOnError(e -> log.error("[FavoriteRepositoryAdapter#exists] DB error: {}", e.getMessage()));
    }

    private Favorite toDomain(FavoriteData d) {
        return Favorite.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .productId(d.getProductId())
                .addedAt(d.getAddedAt())
                .build();
    }

    private FavoriteData toData(Favorite f) {
        return FavoriteData.builder()
                .id(f.getId())
                .userId(f.getUserId())
                .productId(f.getProductId())
                .addedAt(f.getAddedAt())
                .build();
    }
}
