package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class EmitNotificationUseCase {

    private final NotificationGateway notificationGateway;

    public Mono<Notification> execute(UUID userId, String type, String message, String metadata) {
        Notification notification = Notification.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .type(type)
                .message(message)
                .isRead(false)
                .metadata(metadata)
                .createdAt(OffsetDateTime.now())
                .build();
        return notificationGateway.save(notification);
    }
}
