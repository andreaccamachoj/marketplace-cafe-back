package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ListUserNotificationsUseCase {

    private final NotificationGateway notificationGateway;

    public Flux<Notification> execute(UUID userId, int page, int size) {
        return notificationGateway.findByUserId(userId, page, size);
    }

    public Mono<Long> count(UUID userId) {
        return notificationGateway.countByUserId(userId);
    }
}
