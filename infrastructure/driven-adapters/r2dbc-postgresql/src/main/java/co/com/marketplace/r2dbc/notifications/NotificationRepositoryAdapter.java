package co.com.marketplace.r2dbc.notifications;

import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationGateway {

    private final NotificationReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<Notification> save(Notification notification) {
        return repo.save(toData(notification))
                .doOnSubscribe(s -> log.debug("[NotificationRepositoryAdapter#save] DB request: userId={}", notification.getUserId()))
                .doOnSuccess(r -> log.debug("[NotificationRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[NotificationRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<Notification> findByUserId(UUID userId, int page, int size) {
        return repo.findByUserId(userId, size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[NotificationRepositoryAdapter#findByUserId] DB request: userId={}, page={}, size={}", userId, page, size))
                .doOnComplete(() -> log.debug("[NotificationRepositoryAdapter#findByUserId] DB response: complete"))
                .doOnError(e -> log.error("[NotificationRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByUserId(UUID userId) {
        return repo.countByUserId(userId)
                .doOnSubscribe(s -> log.debug("[NotificationRepositoryAdapter#countByUserId] DB request: userId={}", userId))
                .doOnSuccess(r -> log.debug("[NotificationRepositoryAdapter#countByUserId] DB response: result={}", r))
                .doOnError(e -> log.error("[NotificationRepositoryAdapter#countByUserId] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Notification> markRead(UUID id) {
        return db.sql("UPDATE marketplace.notifications SET is_read = TRUE WHERE id = :id RETURNING *")
                .bind("id", id)
                .map((row, meta) -> NotificationData.builder()
                        .id(row.get("id", UUID.class))
                        .userId(row.get("user_id", UUID.class))
                        .type(row.get("type", String.class))
                        .message(row.get("message", String.class))
                        .isRead(Boolean.TRUE.equals(row.get("is_read", Boolean.class)))
                        .metadata(row.get("metadata", String.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[NotificationRepositoryAdapter#markRead] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[NotificationRepositoryAdapter#markRead] DB response: result={}", r != null))
                .doOnError(e -> log.error("[NotificationRepositoryAdapter#markRead] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> markAllRead(UUID userId) {
        return db.sql("UPDATE marketplace.notifications SET is_read = TRUE WHERE user_id = :userId")
                .bind("userId", userId)
                .then()
                .doOnSubscribe(s -> log.debug("[NotificationRepositoryAdapter#markAllRead] DB request: userId={}", userId))
                .doOnTerminate(() -> log.debug("[NotificationRepositoryAdapter#markAllRead] DB response: done"))
                .doOnError(e -> log.error("[NotificationRepositoryAdapter#markAllRead] DB error: {}", e.getMessage()));
    }

    private Notification toDomain(NotificationData d) {
        return Notification.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .type(d.getType())
                .message(d.getMessage())
                .isRead(d.isRead())
                .metadata(d.getMetadata())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private NotificationData toData(Notification n) {
        return NotificationData.builder()
                .id(n.getId())
                .userId(n.getUserId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.isRead())
                .metadata(n.getMetadata())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
