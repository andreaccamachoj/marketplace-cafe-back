package co.com.marketplace.usecase.address;

import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUserAddressesUseCaseTest {

    @Mock private AddressGateway addressGateway;

    @InjectMocks
    private ListUserAddressesUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsAddresses() {
        Address a = Address.builder().id(UUID.randomUUID()).userId(userId).label("Home")
                .line1("Calle 1").city("Bogotá").department("Cundinamarca")
                .isDefault(true).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(addressGateway.findByUserId(userId)).thenReturn(Flux.just(a));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(addr -> "Home".equals(addr.getLabel()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoAddresses() {
        when(addressGateway.findByUserId(userId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyComplete();
    }
}
