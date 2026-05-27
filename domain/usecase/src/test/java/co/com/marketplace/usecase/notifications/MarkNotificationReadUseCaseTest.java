package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkNotificationReadUseCaseTest {

    @Mock private NotificationGateway notificationGateway;

    @InjectMocks
    private MarkNotificationReadUseCase useCase;

    private final UUID notifId = UUID.randomUUID();

    @Test
    void execute_returnsMarkedNotification_whenFound() {
        Notification notif = Notification.builder().id(notifId).userId(UUID.randomUUID())
                .type("ORDER_STATUS").message("Order confirmed").isRead(true)
                .createdAt(OffsetDateTime.now()).build();
        when(notificationGateway.markRead(notifId)).thenReturn(Mono.just(notif));

        StepVerifier.create(useCase.execute(notifId))
                .expectNextMatches(Notification::isRead)
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenNotificationMissing() {
        when(notificationGateway.markRead(notifId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(notifId))
                .verifyError(NotFoundException.class);
    }
}
