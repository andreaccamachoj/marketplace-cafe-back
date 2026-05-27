package co.com.marketplace.model.notifications.gateways;

import co.com.marketplace.model.notifications.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface NotificationGateway {
    Mono<Notification> save(Notification notification);
    Flux<Notification> findByUserId(UUID userId, int page, int size);
    Mono<Long> countByUserId(UUID userId);
    Mono<Notification> markRead(UUID id);
    Mono<Void> markAllRead(UUID userId);
}
