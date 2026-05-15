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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAddressUseCaseTest {

    @Mock private AddressGateway addressGateway;

    @InjectMocks
    private UpdateAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID addressId = UUID.randomUUID();

    private Address buildAddress() {
        return Address.builder().id(addressId).userId(userId).label("Home")
                .line1("Calle 1").city("Bogotá").department("Cundinamarca")
                .isDefault(false).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_returnsUpdatedAddress_whenFound() {
        Address existing = buildAddress();
        Address updated = existing.toBuilder().city("Medellín").build();

        when(addressGateway.findById(addressId)).thenReturn(Mono.just(existing));
        when(addressGateway.update(any())).thenReturn(Mono.just(updated));

        UpdateAddressUseCase.Command cmd = new UpdateAddressUseCase.Command(
                null, null, null, "Medellín", null, null);

        StepVerifier.create(useCase.execute(userId, addressId, cmd))
                .expectNextMatches(a -> "Medellín".equals(a.getCity()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenAddressMissing() {
        when(addressGateway.findById(addressId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, addressId,
                new UpdateAddressUseCase.Command(null, null, null, null, null, null)))
                .verifyError(NotFoundException.class);
    }
}
