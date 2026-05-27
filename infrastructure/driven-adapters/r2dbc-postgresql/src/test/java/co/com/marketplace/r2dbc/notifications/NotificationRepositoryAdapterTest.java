package co.com.marketplace.r2dbc.notifications;

import co.com.marketplace.model.notifications.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class NotificationRepositoryAdapterTest {

    @Mock private NotificationReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private NotificationRepositoryAdapter adapter;

    private final UUID notifId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private NotificationData notifData;
    private Notification notification;

    @BeforeEach
    void setUp() {
        notifData = NotificationData.builder()
                .id(notifId)
                .userId(userId)
                .type("ORDER_STATUS")
                .message("Tu pedido fue confirmado")
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();

        notification = Notification.builder()
                .id(notifId)
                .userId(userId)
                .type("ORDER_STATUS")
                .message("Tu pedido fue confirmado")
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsNotification_whenSuccessful() {
        when(repo.save(any(NotificationData.class))).thenReturn(Mono.just(notifData));

        StepVerifier.create(adapter.save(notification))
                .expectNextMatches(n -> notifId.equals(n.getId()) && "ORDER_STATUS".equals(n.getType()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repo.save(any(NotificationData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(notification))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByUserId_returnsNotifications_whenFound() {
        when(repo.findByUserId(userId, 10, 0L)).thenReturn(Flux.just(notifData));

        StepVerifier.create(adapter.findByUserId(userId, 0, 10))
                .expectNextMatches(n -> userId.equals(n.getUserId()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNone() {
        when(repo.findByUserId(userId, 10, 0L)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByUserId(userId, 0, 10))
                .verifyComplete();
    }

    @Test
    void countByUserId_returnsCount() {
        when(repo.countByUserId(userId)).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.countByUserId(userId))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void markRead_returnsUpdatedNotification() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(notifData)).when(fetchSpec).one();

        StepVerifier.create(adapter.markRead(notifId))
                .expectNextMatches(n -> notifId.equals(n.getId()))
                .verifyComplete();
    }

    @Test
    void markRead_propagatesError_whenDatabaseFails() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.markRead(notifId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void markAllRead_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.markAllRead(userId))
                .verifyComplete();
    }

    @Test
    void markAllRead_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.markAllRead(userId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByUserId_propagatesError() {
        when(repo.findByUserId(userId, 10, 0L)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByUserId(userId, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countByUserId_propagatesError() {
        when(repo.countByUserId(userId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.countByUserId(userId))
                .verifyError(RuntimeException.class);
    }
}
