package co.com.marketplace.api.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProfileRouter {

    @Bean
    public RouterFunction<ServerResponse> profileRoutes(ProfileHandler h) {
        return route()
                .GET("/api/profile/buyer",    h::getBuyerProfile)
                .PATCH("/api/profile/buyer",  accept(MediaType.APPLICATION_JSON), h::patchBuyerProfile)
                .GET("/api/profile/producer", h::getProducerProfile)
                .PATCH("/api/profile/producer", accept(MediaType.APPLICATION_JSON), h::patchProducerProfile)
                .build();
    }
}
