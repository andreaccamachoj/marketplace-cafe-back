package co.com.marketplace.usecase.address;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAddressUseCaseTest {

    @Mock private AddressGateway addressGateway;

    @InjectMocks
    private CreateAddressUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    private Address buildAddress() {
        return Address.builder().id(UUID.randomUUID()).userId(userId).label("Home")
                .line1("Calle 1").city("Bogotá").department("Cundinamarca")
                .isDefault(true).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_clearsDefaultAndSaves_whenIsDefault() {
        when(addressGateway.clearDefaultForUser(userId)).thenReturn(Mono.empty());
        when(addressGateway.save(any())).thenReturn(Mono.just(buildAddress()));

        CreateAddressUseCase.Command cmd = new CreateAddressUseCase.Command(
                "Home", "Calle 1", null, "Bogotá", "Cundinamarca", "110111", true);

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextMatches(a -> "Home".equals(a.getLabel()))
                .verifyComplete();

        verify(addressGateway).clearDefaultForUser(userId);
    }

    @Test
    void execute_savesWithoutClearingDefault_whenNotDefault() {
        when(addressGateway.save(any())).thenReturn(Mono.just(buildAddress().toBuilder().isDefault(false).build()));

        CreateAddressUseCase.Command cmd = new CreateAddressUseCase.Command(
                "Work", "Calle 2", null, "Bogotá", "Cundinamarca", "110112", false);

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextCount(1)
                .verifyComplete();
    }
}
