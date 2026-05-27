package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class MarkAllNotificationsReadUseCase {

    private final NotificationGateway notificationGateway;

    public Mono<Void> execute(UUID userId) {
        return notificationGateway.markAllRead(userId);
    }
}
