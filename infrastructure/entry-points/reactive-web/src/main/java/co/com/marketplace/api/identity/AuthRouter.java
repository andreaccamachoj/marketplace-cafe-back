package co.com.marketplace.api.identity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler h) {
        return route()
                .POST("/api/auth/register/buyer",         accept(MediaType.APPLICATION_JSON), h::registerBuyer)
                .POST("/api/auth/register/producer",      accept(MediaType.APPLICATION_JSON), h::registerProducer)
                .POST("/api/auth/login",                  accept(MediaType.APPLICATION_JSON), h::login)
                .POST("/api/auth/refresh",                accept(MediaType.APPLICATION_JSON), h::refresh)
                .POST("/api/auth/logout",                 h::logout)
                .POST("/api/auth/password-reset/request", accept(MediaType.APPLICATION_JSON), h::requestPasswordReset)
                .POST("/api/auth/password-reset/confirm", accept(MediaType.APPLICATION_JSON), h::confirmPasswordReset)
                .GET("/api/auth/me",                      h::me)
                .PATCH("/api/auth/me/password",           accept(MediaType.APPLICATION_JSON), h::changePassword)
                .POST("/api/auth/consents",               accept(MediaType.APPLICATION_JSON), h::recordConsent)
                .build();
    }
}
