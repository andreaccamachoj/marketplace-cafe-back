package co.com.marketplace.api.notifications;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class NotificationRouter {

    @Bean
    public RouterFunction<ServerResponse> notificationRoutes(NotificationHandler h) {
        return route()
                .GET("/api/notifications",              h::list)
                .PATCH("/api/notifications/{id}/read",  h::markRead)
                .POST("/api/notifications/read-all",    h::markAllRead)
                .build();
    }
}
