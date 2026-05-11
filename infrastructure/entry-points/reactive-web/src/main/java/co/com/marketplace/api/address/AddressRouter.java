package co.com.marketplace.api.address;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AddressRouter {

    @Bean
    public RouterFunction<ServerResponse> addressRoutes(AddressHandler h) {
        return route()
                .GET("/api/addresses",                  h::list)
                .POST("/api/addresses",                 accept(MediaType.APPLICATION_JSON), h::create)
                .PUT("/api/addresses/{id}",             accept(MediaType.APPLICATION_JSON), h::update)
                .DELETE("/api/addresses/{id}",          h::delete)
                .PATCH("/api/addresses/{id}/default",   h::setDefault)
                .build();
    }
}
