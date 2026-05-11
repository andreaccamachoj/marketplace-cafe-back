package co.com.marketplace.api.notifications;

import co.com.marketplace.usecase.notifications.ListUserNotificationsUseCase;
import co.com.marketplace.usecase.notifications.MarkAllNotificationsReadUseCase;
import co.com.marketplace.usecase.notifications.MarkNotificationReadUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationHandler {

    private final ListUserNotificationsUseCase listUserNotificationsUseCase;
    private final MarkNotificationReadUseCase markNotificationReadUseCase;
    private final MarkAllNotificationsReadUseCase markAllNotificationsReadUseCase;

    public Mono<ServerResponse> list(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return userId(request)
                .flatMapMany(uid -> listUserNotificationsUseCase.execute(uid, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> markRead(ServerRequest request) {
        UUID notifId = UUID.fromString(request.pathVariable("id"));
        return markNotificationReadUseCase.execute(notifId)
                .flatMap(notif -> ServerResponse.ok().bodyValue(notif));
    }

    public Mono<ServerResponse> markAllRead(ServerRequest request) {
        return userId(request)
                .flatMap(markAllNotificationsReadUseCase::execute)
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
