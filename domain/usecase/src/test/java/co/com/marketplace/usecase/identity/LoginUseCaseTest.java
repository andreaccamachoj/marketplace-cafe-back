package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.AuthTokens;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserGateway userGateway;
    @Mock
    private RoleGateway roleGateway;
    @Mock
    private PasswordEncoderGateway passwordEncoder;
    @Mock
    private TokenProviderGateway tokenProvider;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private final UUID userId = UUID.randomUUID();

    private User buildUser() {
        return User.builder()
                .id(userId)
                .email("test@example.com")
                .hashedPassword("hashed")
                .fullName("Test User")
                .phone("123")
                .status(UserStatus.active)
                .privacyConsent(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void execute_returnsTokens_whenCredentialsValid() {
        User user = buildUser();
        Role role = Role.builder().id(1).name("BUYER").description("Buyer").build();

        when(userGateway.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("rawPass", "hashed")).thenReturn(true);
        when(roleGateway.findByUserId(userId)).thenReturn(Flux.just(role));
        when(tokenProvider.generateAccessToken(userId, "test@example.com", "BUYER")).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(userId)).thenReturn("refresh-token");

        StepVerifier.create(loginUseCase.execute("test@example.com", "rawPass"))
                .expectNextMatches(tokens -> "access-token".equals(tokens.accessToken()) && "refresh-token".equals(tokens.refreshToken()))
                .verifyComplete();
    }

    @Test
    void execute_throwsUnauthorized_whenUserNotFound() {
        when(userGateway.findByEmail("nobody@example.com")).thenReturn(Mono.empty());

        StepVerifier.create(loginUseCase.execute("nobody@example.com", "anyPass"))
                .verifyError(UnauthorizedException.class);
    }

    @Test
    void execute_throwsUnauthorized_whenPasswordWrong() {
        User user = buildUser();
        when(userGateway.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(passwordEncoder.matches("wrongPass", "hashed")).thenReturn(false);

        StepVerifier.create(loginUseCase.execute("test@example.com", "wrongPass"))
                .verifyError(UnauthorizedException.class);
    }
}
