package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.AuthTokens;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class LoginUseCase {

    private final UserGateway userGateway;
    private final RoleGateway roleGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenProviderGateway tokenProvider;

    public Mono<AuthTokens> execute(String email, String rawPassword) {
        return userGateway.findByEmail(email)
                .switchIfEmpty(Mono.error(new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                        "Credenciales inválidas")))
                .flatMap(user -> Mono.fromCallable(() -> passwordEncoder.matches(rawPassword, user.getHashedPassword()))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                                "Credenciales inválidas")))
                        .flatMap(__ -> roleGateway.findByUserId(user.getId()).next()
                                .map(role -> new AuthTokens(
                                        tokenProvider.generateAccessToken(user.getId(), user.getEmail(), role.getName()),
                                        tokenProvider.generateRefreshToken(user.getId())
                                ))));
    }
}
