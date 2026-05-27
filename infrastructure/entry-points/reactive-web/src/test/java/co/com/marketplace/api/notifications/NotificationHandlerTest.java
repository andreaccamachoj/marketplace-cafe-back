package co.com.marketplace.api.notifications;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.notifications.Notification;
import co.com.marketplace.usecase.notifications.ListUserNotificationsUseCase;
import co.com.marketplace.usecase.notifications.MarkAllNotificationsReadUseCase;
import co.com.marketplace.usecase.notifications.MarkNotificationReadUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {NotificationRouter.class, NotificationHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class NotificationHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListUserNotificationsUseCase listUserNotificationsUseCase;
    @MockitoBean private MarkNotificationReadUseCase markNotificationReadUseCase;
    @MockitoBean private MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

    private Notification buildNotification() {
        return Notification.builder()
                .id(UUID.randomUUID()).userId(UUID.fromString(USER_ID))
                .type("INFO").message("Test").isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void list_returns200() {
        when(listUserNotificationsUseCase.execute(any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildNotification()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/notifications")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void markRead_returns200() {
        when(markNotificationReadUseCase.execute(any())).thenReturn(Mono.just(buildNotification()));

        webTestClient.patch().uri("/api/notifications/" + UUID.randomUUID() + "/read")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void markAllRead_returns204() {
        when(markAllNotificationsReadUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/notifications/read-all")
                .exchange()
                .expectStatus().isNoContent();
    }
}
