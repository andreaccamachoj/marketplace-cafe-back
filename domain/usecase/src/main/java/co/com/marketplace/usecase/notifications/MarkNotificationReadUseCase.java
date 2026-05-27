package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class MarkNotificationReadUseCase {

    private final NotificationGateway notificationGateway;

    public Mono<Notification> execute(UUID notificationId) {
        return notificationGateway.markRead(notificationId)
                .switchIfEmpty(Mono.error(new NotFoundException("NOTIFICATION_NOT_FOUND", "Notification not found: " + notificationId)));
    }
}
