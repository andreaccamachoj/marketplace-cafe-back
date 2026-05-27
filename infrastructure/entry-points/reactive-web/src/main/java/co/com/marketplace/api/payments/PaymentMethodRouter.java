package co.com.marketplace.api.payments;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PaymentMethodRouter {

    @Bean
    public RouterFunction<ServerResponse> paymentMethodRoutes(PaymentMethodHandler h) {
        return route()
                .GET("/api/payment-methods", h::list)
                .build();
    }
}
