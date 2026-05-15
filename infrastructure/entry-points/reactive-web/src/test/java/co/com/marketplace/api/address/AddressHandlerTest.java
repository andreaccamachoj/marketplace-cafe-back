package co.com.marketplace.api.address;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.model.identity.Address;
import co.com.marketplace.usecase.address.CreateAddressUseCase;
import co.com.marketplace.usecase.address.DeleteAddressUseCase;
import co.com.marketplace.usecase.address.ListUserAddressesUseCase;
import co.com.marketplace.usecase.address.SetDefaultAddressUseCase;
import co.com.marketplace.usecase.address.UpdateAddressUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.transaction.reactive.TransactionalOperator;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {AddressRouter.class, AddressHandler.class, GlobalErrorWebExceptionHandler.class})
class AddressHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;
    @Autowired private org.springframework.transaction.reactive.TransactionalOperator injectedTx;

    @MockitoBean private TransactionalOperator tx;
    @MockitoBean private ListUserAddressesUseCase listUserAddressesUseCase;
    @MockitoBean private CreateAddressUseCase createAddressUseCase;
    @MockitoBean private UpdateAddressUseCase updateAddressUseCase;
    @MockitoBean private DeleteAddressUseCase deleteAddressUseCase;
    @MockitoBean private SetDefaultAddressUseCase setDefaultAddressUseCase;

    @org.junit.jupiter.api.BeforeEach
    void checkTx() {
        System.out.println(">>> injectedTx class: " + injectedTx.getClass().getName());
    }

    private Address buildAddress() {
        return Address.builder()
                .id(UUID.randomUUID()).userId(UUID.fromString(USER_ID))
                .label("Casa").line1("Calle 1").city("Bogota").department("Cundinamarca")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void list_returns200() {
        when(listUserAddressesUseCase.execute(any())).thenReturn(Flux.just(buildAddress()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/addresses")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void update_returns200() {
        when(updateAddressUseCase.execute(any(), any(), any())).thenAnswer(i -> {
            System.out.println(">>> updateAddressUseCase.execute() called!!!");
            return Mono.just(buildAddress());
        });

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .put().uri("/api/addresses/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(java.util.Map.of("label", "Casa", "line1", "Calle 1", "city", "Bogota",
                        "department", "Cund", "zipCode", "111111"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void delete_returns204() {
        when(deleteAddressUseCase.execute(any(), any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .delete().uri("/api/addresses/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void setDefault_returns204() {
        when(setDefaultAddressUseCase.execute(any(), any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/addresses/" + UUID.randomUUID() + "/default")
                .exchange()
                .expectStatus().isNoContent();
    }
}
