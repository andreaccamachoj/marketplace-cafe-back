package co.com.marketplace.usecase.notifications;

import co.com.marketplace.model.notifications.gateways.NotificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkAllNotificationsReadUseCaseTest {

    @Mock private NotificationGateway notificationGateway;

    @InjectMocks
    private MarkAllNotificationsReadUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_marksAllAsRead() {
        when(notificationGateway.markAllRead(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyComplete();

        verify(notificationGateway).markAllRead(userId);
    }

    @Test
    void execute_propagatesError_whenGatewayFails() {
        when(notificationGateway.markAllRead(userId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.execute(userId))
                .verifyError(RuntimeException.class);
    }
}
