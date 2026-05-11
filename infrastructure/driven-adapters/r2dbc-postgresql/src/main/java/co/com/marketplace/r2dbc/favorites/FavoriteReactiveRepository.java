package co.com.marketplace.r2dbc.favorites;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FavoriteReactiveRepository extends ReactiveCrudRepository<FavoriteData, UUID> {
    @Query("SELECT * FROM marketplace.favorites WHERE user_id = :userId")
    Flux<FavoriteData> findByUserId(UUID userId);
}
