package co.com.marketplace.r2dbc.notifications;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NotificationReactiveRepository extends ReactiveCrudRepository<NotificationData, UUID> {
    @Query("SELECT * FROM marketplace.notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<NotificationData> findByUserId(UUID userId, int size, long offset);

    @Query("SELECT COUNT(*) FROM marketplace.notifications WHERE user_id = :userId")
    Mono<Long> countByUserId(UUID userId);
}
