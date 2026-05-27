package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
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
class LinkFarmCertificationUseCaseTest {

    @Mock private FarmGateway farmGateway;

    @InjectMocks
    private LinkFarmCertificationUseCase useCase;

    private final UUID producerId = UUID.randomUUID();
    private final UUID farmId = UUID.randomUUID();

    @Test
    void execute_savesCertification_whenFarmFound() {
        Farm farm = Farm.builder().id(farmId).producerId(producerId).name("Farm")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        FarmCertification saved = FarmCertification.builder().id(UUID.randomUUID())
                .farmId(farmId).certificationId(1).issuer("USDA").status(DocStatus.pending).build();

        when(farmGateway.findByProducerId(producerId)).thenReturn(Mono.just(farm));
        when(farmGateway.saveCertification(any())).thenReturn(Mono.just(saved));

        LinkFarmCertificationUseCase.Command cmd = new LinkFarmCertificationUseCase.Command(
                1, "USDA", LocalDate.now(), LocalDate.now().plusYears(1), null);

        StepVerifier.create(useCase.execute(producerId, cmd))
                .expectNextMatches(c -> DocStatus.pending.equals(c.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenFarmMissing() {
        when(farmGateway.findByProducerId(producerId)).thenReturn(Mono.empty());

        LinkFarmCertificationUseCase.Command cmd = new LinkFarmCertificationUseCase.Command(
                1, "USDA", LocalDate.now(), LocalDate.now().plusYears(1), null);

        StepVerifier.create(useCase.execute(producerId, cmd))
                .verifyError(NotFoundException.class);
    }
}
