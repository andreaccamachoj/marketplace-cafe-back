package co.com.marketplace.api.address;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
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
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {AddressRouter.class, AddressHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class AddressHandlerCreateTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private TransactionalOperator tx;
    @MockitoBean private ListUserAddressesUseCase listUserAddressesUseCase;
    @MockitoBean private CreateAddressUseCase createAddressUseCase;
    @MockitoBean private UpdateAddressUseCase updateAddressUseCase;
    @MockitoBean private DeleteAddressUseCase deleteAddressUseCase;
    @MockitoBean private SetDefaultAddressUseCase setDefaultAddressUseCase;

    private Address buildAddress() {
        return Address.builder()
                .id(UUID.randomUUID()).userId(UUID.fromString(USER_ID))
                .label("Casa").line1("Calle 1").city("Bogota").department("Cundinamarca")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void create_returns2xx() {
        when(createAddressUseCase.execute(any(), any())).thenReturn(Mono.just(buildAddress()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"label":"Casa","line1":"Calle 1","city":"Bogota",
                         "department":"Cundinamarca","zipCode":"111111"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
