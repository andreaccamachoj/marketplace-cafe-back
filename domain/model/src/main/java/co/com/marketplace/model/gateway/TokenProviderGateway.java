package co.com.marketplace.model.gateway;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TokenProviderGateway {

    String generateAccessToken(UUID userId, String email, String role);

    String generateRefreshToken(UUID userId);

    Mono<UUID> validateToken(String token);

    boolean isTokenValid(String token);

    String extractRole(String token);
}
