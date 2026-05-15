package co.com.marketplace.usecase.address;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.Address;
import co.com.marketplace.model.identity.gateways.AddressGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAddressUseCaseTest {

    @Mock private AddressGateway addressGateway;

    @InjectMocks
    private DeleteAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    @Test
    void execute_deletesSuccessfully_whenFound() {
        Address address = Address.builder().id(addressId).userId(userId).label("Home")
                .line1("Calle 1").city("Bogotá").department("Cundinamarca")
                .isDefault(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(addressGateway.findById(addressId)).thenReturn(Mono.just(address));
        when(addressGateway.deleteById(addressId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, addressId))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenAddressMissing() {
        when(addressGateway.findById(addressId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, addressId))
                .verifyError(NotFoundException.class);
    }
}
