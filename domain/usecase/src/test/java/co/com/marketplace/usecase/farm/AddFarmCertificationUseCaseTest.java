package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.shared.DocStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddFarmCertificationUseCaseTest {

    @Mock private FarmGateway farmGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private AddFarmCertificationUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID farmId = UUID.randomUUID();

    @Test
    void execute_addsCertification_whenFarmFound() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Farm farm = Farm.builder().id(farmId).producerId(profileId).name("Farm")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        FarmCertification saved = FarmCertification.builder().id(UUID.randomUUID())
                .farmId(farmId).status(DocStatus.approved).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(farmGateway.findByProducerId(profileId)).thenReturn(Mono.just(farm));
        when(farmGateway.saveCertification(any())).thenReturn(Mono.just(saved));

        AddFarmCertificationUseCase.Command cmd = new AddFarmCertificationUseCase.Command(
                "organic", "Organic Cert", "USDA", LocalDate.now().plusYears(1));

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextMatches(c -> DocStatus.approved.equals(c.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        AddFarmCertificationUseCase.Command cmd = new AddFarmCertificationUseCase.Command(
                "organic", "Organic Cert", "USDA", LocalDate.now().plusYears(1));

        StepVerifier.create(useCase.execute(userId, cmd))
                .verifyError(NotFoundException.class);
    }
}
