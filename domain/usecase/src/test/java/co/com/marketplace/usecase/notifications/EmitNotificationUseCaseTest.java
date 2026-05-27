package co.com.marketplace.usecase.notifications;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmitNotificationUseCaseTest {

    @Mock private NotificationGateway notificationGateway;

    @InjectMocks
    private EmitNotificationUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_savesAndReturnsNotification() {
        Notification saved = Notification.builder().id(UUID.randomUUID()).userId(userId)
                .type("ORDER_STATUS").message("Your order was confirmed!").isRead(false)
                .createdAt(OffsetDateTime.now()).build();
        when(notificationGateway.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute(userId, "ORDER_STATUS", "Your order was confirmed!", null))
                .expectNextMatches(n -> "ORDER_STATUS".equals(n.getType()) && !n.isRead())
                .verifyComplete();
    }

    @Test
    void execute_propagatesError_whenGatewayFails() {
        when(notificationGateway.save(any())).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.execute(userId, "TYPE", "msg", null))
                .verifyError(RuntimeException.class);
    }
}
