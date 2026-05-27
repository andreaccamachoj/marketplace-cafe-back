package co.com.marketplace.api.cart;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CartRouter {

    @Bean
    public RouterFunction<ServerResponse> cartRoutes(CartHandler h) {
        return route()
                .GET("/api/cart",                       h::getCart)
                .POST("/api/cart/items",                accept(MediaType.APPLICATION_JSON), h::addItem)
                .PATCH("/api/cart/items/{itemId}",      accept(MediaType.APPLICATION_JSON), h::updateItem)
                .DELETE("/api/cart/items/{itemId}",     h::removeItem)
                .DELETE("/api/cart",                    h::clearCart)
                .POST("/api/cart/coupon",               accept(MediaType.APPLICATION_JSON), h::applyCoupon)
                .DELETE("/api/cart/coupon",             h::removeCoupon)
                .PATCH("/api/cart/shipping",            accept(MediaType.APPLICATION_JSON), h::selectShipping)
                .build();
    }
}
