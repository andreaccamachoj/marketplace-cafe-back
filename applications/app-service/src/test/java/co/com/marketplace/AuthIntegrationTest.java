package co.com.marketplace;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.SecurityConfig;
import co.com.marketplace.api.identity.AuthHandler;
import co.com.marketplace.api.identity.AuthRouter;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.identity.AuthTokens;
import co.com.marketplace.model.identity.Role;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.RoleGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import co.com.marketplace.usecase.identity.ChangePasswordUseCase;
import co.com.marketplace.usecase.identity.ConfirmPasswordResetUseCase;
import co.com.marketplace.usecase.identity.GetCurrentUserUseCase;
import co.com.marketplace.usecase.identity.LoginUseCase;
import co.com.marketplace.usecase.identity.LogoutUseCase;
import co.com.marketplace.usecase.identity.RecordPrivacyConsentUseCase;
import co.com.marketplace.usecase.identity.RefreshTokenUseCase;
import co.com.marketplace.usecase.identity.RegisterBuyerUseCase;
import co.com.marketplace.usecase.identity.RegisterProducerUseCase;
import co.com.marketplace.usecase.identity.RequestPasswordResetUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        AuthRouter.class,
        AuthHandler.class,
        SecurityConfig.class,
        GlobalErrorWebExceptionHandler.class,
        AuthIntegrationTest.RealUseCasesConfig.class
})
class AuthIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private UserGateway userGateway;
    @MockitoBean private RoleGateway roleGateway;
    @MockitoBean private PasswordEncoderGateway passwordEncoderGateway;
    @MockitoBean private TokenProviderGateway tokenProviderGateway;

    @MockitoBean private RegisterBuyerUseCase registerBuyerUseCase;
    @MockitoBean private RegisterProducerUseCase registerProducerUseCase;
    @MockitoBean private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    @MockitoBean private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean private ChangePasswordUseCase changePasswordUseCase;
    @MockitoBean private RecordPrivacyConsentUseCase recordPrivacyConsentUseCase;
    @MockitoBean private TransactionalOperator tx;

    @TestConfiguration
    static class RealUseCasesConfig {
        @Bean
        LoginUseCase loginUseCase(UserGateway userGateway, RoleGateway roleGateway,
                                   PasswordEncoderGateway passwordEncoder,
                                   TokenProviderGateway tokenProvider) {
            return new LoginUseCase(userGateway, roleGateway, passwordEncoder, tokenProvider);
        }

        @Bean
        RefreshTokenUseCase refreshTokenUseCase(TokenProviderGateway tokenProvider,
                                                 UserGateway userGateway, RoleGateway roleGateway) {
            return new RefreshTokenUseCase(tokenProvider, userGateway, roleGateway);
        }

        @Bean
        LogoutUseCase logoutUseCase() {
            return new LogoutUseCase();
        }
    }

    record LoginRequest(String email, String password) {}
    record RefreshRequest(String refreshToken) {}

    @Test
    void login_validCredentials_returns200WithTokens() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId).email("buyer@test.com").hashedPassword("hashed")
                .fullName("Test").phone("555").status(UserStatus.active)
                .build();
        Role role = Role.builder().id(1).name("BUYER").build();

        when(userGateway.findByEmail("buyer@test.com")).thenReturn(Mono.just(user));
        when(passwordEncoderGateway.matches("pass123", "hashed")).thenReturn(true);
        when(roleGateway.findByUserId(userId)).thenReturn(Flux.just(role));
        when(tokenProviderGateway.generateAccessToken(userId, "buyer@test.com", "BUYER"))
                .thenReturn("access-token");
        when(tokenProviderGateway.generateRefreshToken(userId)).thenReturn("refresh-token");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("buyer@test.com", "pass123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token");
    }

    @Test
    void login_invalidCredentials_returns401() {
        when(userGateway.findByEmail("bad@test.com")).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("bad@test.com", "wrong"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void refresh_validToken_returns200WithNewTokens() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId).email("buyer@test.com").hashedPassword("hashed")
                .fullName("Test").phone("555").status(UserStatus.active)
                .build();
        Role role = Role.builder().id(1).name("BUYER").build();

        when(tokenProviderGateway.isTokenValid("valid-refresh-token")).thenReturn(true);
        when(tokenProviderGateway.validateToken("valid-refresh-token"))
                .thenReturn(Mono.just(userId));
        when(userGateway.findById(userId)).thenReturn(Mono.just(user));
        when(roleGateway.findByUserId(userId)).thenReturn(Flux.just(role));
        when(tokenProviderGateway.generateAccessToken(userId, "buyer@test.com", "BUYER"))
                .thenReturn("new-access-token");
        when(tokenProviderGateway.generateRefreshToken(userId)).thenReturn("new-refresh-token");

        webTestClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new RefreshRequest("valid-refresh-token"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-access-token")
                .jsonPath("$.refreshToken").isEqualTo("new-refresh-token");
    }

    @Test
    void logout_validToken_returns204() {
        UUID userId = UUID.randomUUID();
        String bearerToken = "valid-access-token";

        when(tokenProviderGateway.isTokenValid(bearerToken)).thenReturn(true);
        when(tokenProviderGateway.validateToken(bearerToken)).thenReturn(Mono.just(userId));
        when(tokenProviderGateway.extractRole(bearerToken)).thenReturn("BUYER");

        webTestClient.post()
                .uri("/api/auth/logout")
                .header("Authorization", "Bearer " + bearerToken)
                .exchange()
                .expectStatus().isNoContent();
    }
}
