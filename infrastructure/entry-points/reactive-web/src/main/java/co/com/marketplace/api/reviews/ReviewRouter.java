package co.com.marketplace.api.reviews;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

    @Bean
    public RouterFunction<ServerResponse> reviewRoutes(ReviewHandler h) {
        return route()
                .GET("/api/reviews",                                                        h::listMyReviews)
                .POST("/api/reviews",                   accept(MediaType.APPLICATION_JSON), h::create)
                .POST("/api/reviews/{id}/reply",        accept(MediaType.APPLICATION_JSON), h::reply)
                .PATCH("/api/reviews/{id}/moderate",    accept(MediaType.APPLICATION_JSON), h::moderate)
                .build();
    }
}
