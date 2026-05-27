package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserNotificationsUseCaseTest {

    @Mock private NotificationGateway notificationGateway;

    @InjectMocks
    private ListUserNotificationsUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsNotifications() {
        Notification n = Notification.builder().id(UUID.randomUUID()).userId(userId)
                .type("ORDER_STATUS").message("Order confirmed").isRead(false)
                .createdAt(OffsetDateTime.now()).build();
        when(notificationGateway.findByUserId(userId, 0, 20)).thenReturn(Flux.just(n));

        StepVerifier.create(useCase.execute(userId, 0, 20))
                .expectNextMatches(notif -> !notif.isRead())
                .verifyComplete();
    }

    @Test
    void count_returnsCount() {
        when(notificationGateway.countByUserId(userId)).thenReturn(Mono.just(3L));

        StepVerifier.create(useCase.count(userId))
                .expectNext(3L)
                .verifyComplete();
    }
}
