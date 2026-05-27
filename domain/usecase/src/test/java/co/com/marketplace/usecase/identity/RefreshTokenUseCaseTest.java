package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.Role;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    @Mock private TokenProviderGateway tokenProvider;
    @Mock private UserGateway userGateway;
    @Mock private RoleGateway roleGateway;

    @InjectMocks
    private RefreshTokenUseCase refreshTokenUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsNewTokens_whenRefreshTokenValid() {
        User user = User.builder().id(userId).email("u@e.com").fullName("U").status(UserStatus.active)
                .privacyConsent(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Role role = Role.builder().id(1).name("BUYER").description("Buyer").build();

        when(tokenProvider.isTokenValid("rt")).thenReturn(true);
        when(tokenProvider.validateToken("rt")).thenReturn(Mono.just(userId));
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(roleGateway.findByUserId(userId)).thenReturn(Flux.just(role));
        when(tokenProvider.generateAccessToken(userId, "u@e.com", "BUYER")).thenReturn("at");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("rt2");

        StepVerifier.create(refreshTokenUseCase.execute("rt"))
                .expectNextMatches(t -> "at".equals(t.accessToken()) && "rt2".equals(t.refreshToken()))
                .verifyComplete();
    }

    @Test
    void execute_throwsUnauthorized_whenTokenInvalid() {
        when(tokenProvider.isTokenValid("bad")).thenReturn(false);

        StepVerifier.create(refreshTokenUseCase.execute("bad"))
                .verifyError(UnauthorizedException.class);
    }

    @Test
    void execute_throwsUnauthorized_whenUserNotFound() {
        when(tokenProvider.isTokenValid("rt")).thenReturn(true);
        when(tokenProvider.validateToken("rt")).thenReturn(Mono.just(userId));
        when(userGateway.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(refreshTokenUseCase.execute("rt"))
                .verifyError(UnauthorizedException.class);
    }
}
