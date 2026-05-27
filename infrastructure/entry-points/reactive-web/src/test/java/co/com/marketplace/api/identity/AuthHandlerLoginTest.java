package co.com.marketplace.api.identity;

import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.identity.AuthTokens;
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
import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {AuthRouter.class, AuthHandler.class, GlobalErrorWebExceptionHandler.class})
class AuthHandlerLoginTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private RegisterBuyerUseCase registerBuyerUseCase;
    @MockitoBean private RegisterProducerUseCase registerProducerUseCase;
    @MockitoBean private LoginUseCase loginUseCase;
    @MockitoBean private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean private LogoutUseCase logoutUseCase;
    @MockitoBean private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    @MockitoBean private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean private ChangePasswordUseCase changePasswordUseCase;
    @MockitoBean private RecordPrivacyConsentUseCase recordPrivacyConsentUseCase;
    @MockitoBean private TransactionalOperator tx;

    record LoginRequest(String email, String password) {}

    @Test
    void login_returns200_withValidCredentials() {
        AuthTokens tokens = new AuthTokens("access-token", "refresh-token");
        when(loginUseCase.execute("valid@example.com", "pass123")).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("valid@example.com", "pass123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token");
    }

    @Test
    void login_returns401_withInvalidCredentials() {
        when(loginUseCase.execute("bad@example.com", "wrongpass"))
                .thenReturn(Mono.error(new UnauthorizedException("AUTH_INVALID_CREDENTIALS", "Credenciales inválidas")));

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new LoginRequest("bad@example.com", "wrongpass"))
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
