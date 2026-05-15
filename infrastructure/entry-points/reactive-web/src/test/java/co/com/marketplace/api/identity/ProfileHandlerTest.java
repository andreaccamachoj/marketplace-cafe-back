package co.com.marketplace.api.identity;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.usecase.identity.GetBuyerProfileUseCase;
import co.com.marketplace.usecase.identity.GetProducerProfileUseCase;
import co.com.marketplace.usecase.identity.UpdateBuyerProfileUseCase;
import co.com.marketplace.usecase.identity.UpdateProducerProfileUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ProfileRouter.class, ProfileHandler.class, GlobalErrorWebExceptionHandler.class})
class ProfileHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private GetBuyerProfileUseCase getBuyerProfileUseCase;
    @MockitoBean private GetProducerProfileUseCase getProducerProfileUseCase;
    @MockitoBean private UpdateBuyerProfileUseCase updateBuyerProfileUseCase;
    @MockitoBean private UpdateProducerProfileUseCase updateProducerProfileUseCase;

    @Test
    void getBuyerProfile_returns200() {
        when(getBuyerProfileUseCase.execute(any())).thenReturn(
                Mono.just(new GetBuyerProfileUseCase.Result(
                        UUID.fromString(USER_ID), "test@test.com", "Test User",
                        null, null, null, null, false, null)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/profile/buyer")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void patchBuyerProfile_returns200() {
        when(updateBuyerProfileUseCase.execute(any(), any())).thenReturn(
                Mono.just(new UpdateBuyerProfileUseCase.Result(
                        UUID.fromString(USER_ID), "test@test.com", "Updated",
                        null, null, null, null, false, null)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/profile/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"fullName":"Updated"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getProducerProfile_returns200() {
        var result = Mockito.mock(GetProducerProfileUseCase.Result.class);
        when(getProducerProfileUseCase.execute(any())).thenReturn(Mono.just(result));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/profile/producer")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void patchProducerProfile_returns200() {
        var result = Mockito.mock(UpdateProducerProfileUseCase.Result.class);
        when(updateProducerProfileUseCase.execute(any(), any())).thenReturn(Mono.just(result));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/profile/producer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"fullName":"Updated"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }
}
