package co.com.marketplace.api.identity;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {AuthRouter.class, AuthHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class AuthHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

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

    private GetCurrentUserUseCase.Result buildCurrentUser() {
        return new GetCurrentUserUseCase.Result(
                UUID.fromString(USER_ID), "test@test.com", "Test", null, "active", null, null);
    }

    @Test
    void registerBuyer_returns2xx() {
        AuthTokens tokens = new AuthTokens("access", "refresh");
        when(registerBuyerUseCase.execute(any())).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri("/api/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email":"buyer@test.com","password":"pass123","fullName":"Buyer","phone":"123"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void registerProducer_returns2xx() {
        AuthTokens tokens = new AuthTokens("access-p", "refresh-p");
        when(registerProducerUseCase.execute(any())).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri("/api/auth/register/producer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email":"prod@test.com","password":"pass123","fullName":"Prod","phone":"456",
                         "bio":"bio","city":"Bogota","department":"Cundinamarca"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void refresh_returns200() {
        AuthTokens tokens = new AuthTokens("new-access", "new-refresh");
        when(refreshTokenUseCase.execute(anyString())).thenReturn(Mono.just(tokens));

        webTestClient.post()
                .uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"refreshToken":"old-refresh-token"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("new-access");
    }

    @Test
    void logout_returns204() {
        when(logoutUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/auth/logout")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void me_returns2xx() {
        when(getCurrentUserUseCase.execute(any())).thenReturn(Mono.just(buildCurrentUser()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/auth/me")
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void changePassword_returns2xx() {
        when(changePasswordUseCase.execute(any(), anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/auth/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"oldPassword":"old123","newPassword":"new456"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void requestPasswordReset_returns202() {
        when(requestPasswordResetUseCase.execute(anyString())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"email":"user@test.com"}
                        """)
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    void confirmPasswordReset_returns204() {
        when(confirmPasswordResetUseCase.execute(anyString(), anyString())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"token":"reset-token-123","newPassword":"newpass456"}
                        """)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void recordConsent_returns2xx() {
        when(recordPrivacyConsentUseCase.execute(any(), anyString(), any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/auth/consents")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"policyVersion":"v1.0"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
