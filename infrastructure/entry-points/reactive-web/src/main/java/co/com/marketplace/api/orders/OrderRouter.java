package co.com.marketplace.api.orders;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class OrderRouter {

    @Bean
    public RouterFunction<ServerResponse> orderRoutes(OrderHandler h) {
        return route()
                .POST("/api/orders",                            accept(MediaType.APPLICATION_JSON), h::placeOrder)
                .GET("/api/orders",                             h::listOrders)
                .GET("/api/orders/{id}",                        h::getOrder)
                .POST("/api/orders/{id}/cancel",                accept(MediaType.APPLICATION_JSON), h::cancelOrder)
                .GET("/api/orders/{id}/timeline",               h::getTimeline)
                .GET("/api/orders/{id}/invoice",                h::getInvoice)
                .POST("/api/orders/{id}/payment-proof",         accept(MediaType.APPLICATION_JSON), h::submitPaymentProof)
                .GET("/api/orders/{id}/payment",                h::getPayment)
                .build();
    }
}
