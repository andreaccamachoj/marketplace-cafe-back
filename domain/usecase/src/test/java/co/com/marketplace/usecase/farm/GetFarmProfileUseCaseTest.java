package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
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
class GetFarmProfileUseCaseTest {

    @Mock private FarmGateway farmGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private GetFarmProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Test
    void execute_returnsFarm_whenBothFound() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Farm farm = Farm.builder().id(UUID.randomUUID()).producerId(profileId).name("La Finca")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(farmGateway.findByProducerId(profileId)).thenReturn(Mono.just(farm));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(f -> "La Finca".equals(f.getName()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyError(NotFoundException.class);
    }

    @Test
    void execute_throwsNotFound_whenFarmMissing() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(farmGateway.findByProducerId(profileId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyError(NotFoundException.class);
    }
}
