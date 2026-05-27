package co.com.marketplace.api.favorites;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class FavoritesRouter {

    @Bean
    public RouterFunction<ServerResponse> favoritesRoutes(FavoritesHandler h) {
        return route()
                .GET("/api/favorites",                  h::list)
                .POST("/api/favorites/{productId}",     h::add)
                .DELETE("/api/favorites/{productId}",   h::remove)
                .build();
    }
}
