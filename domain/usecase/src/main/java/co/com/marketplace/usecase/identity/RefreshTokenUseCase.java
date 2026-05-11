package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.AuthTokens;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class RefreshTokenUseCase {

    private final TokenProviderGateway tokenProvider;
    private final UserGateway userGateway;
    private final RoleGateway roleGateway;

    public Mono<AuthTokens> execute(String refreshToken) {
        if (!tokenProvider.isTokenValid(refreshToken)) {
            return Mono.error(new UnauthorizedException("AUTH_INVALID_TOKEN", "Token de refresco inválido"));
        }
        return tokenProvider.validateToken(refreshToken)
                .flatMap(userId -> userGateway.findById(userId)
                        .switchIfEmpty(Mono.error(new UnauthorizedException("AUTH_INVALID_TOKEN",
                                "Usuario no encontrado")))
                        .flatMap(user -> roleGateway.findByUserId(userId).next()
                                .map(role -> new AuthTokens(
                                        tokenProvider.generateAccessToken(userId, user.getEmail(), role.getName()),
                                        tokenProvider.generateRefreshToken(userId)
                                ))));
    }
}
