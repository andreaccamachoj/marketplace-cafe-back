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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFarmProfileUseCaseTest {

    @Mock private FarmGateway farmGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private UpdateFarmProfileUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    private UpdateFarmProfileUseCase.Command cmd() {
        return new UpdateFarmProfileUseCase.Command("La Finca Updated", "Salento", "Quindío",
                BigDecimal.valueOf(1800), BigDecimal.valueOf(5), "Geisha", "Washed", "Beautiful farm");
    }

    @Test
    void execute_updatesFarm_whenExists() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Farm existing = Farm.builder().id(UUID.randomUUID()).producerId(profileId).name("La Finca")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Farm updated = existing.toBuilder().name("La Finca Updated").build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(farmGateway.findByProducerId(profileId)).thenReturn(Mono.just(existing));
        when(farmGateway.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(userId, cmd()))
                .expectNextMatches(f -> "La Finca Updated".equals(f.getName()))
                .verifyComplete();
    }

    @Test
    void execute_createsFarm_whenDoesNotExist() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Farm newFarm = Farm.builder().id(UUID.randomUUID()).producerId(profileId).name("La Finca Updated")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(farmGateway.findByProducerId(profileId)).thenReturn(Mono.empty());
        when(farmGateway.save(any())).thenReturn(Mono.just(newFarm));

        StepVerifier.create(useCase.execute(userId, cmd()))
                .expectNextMatches(f -> profileId.equals(f.getProducerId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, cmd()))
                .verifyError(NotFoundException.class);
    }
}
