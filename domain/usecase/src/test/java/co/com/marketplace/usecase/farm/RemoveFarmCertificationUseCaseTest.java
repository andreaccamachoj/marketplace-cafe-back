package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.farm.gateways.FarmGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveFarmCertificationUseCaseTest {

    @Mock private FarmGateway farmGateway;

    @InjectMocks
    private RemoveFarmCertificationUseCase useCase;

    private final UUID certId = UUID.randomUUID();

    @Test
    void execute_deletesSuccessfully() {
        when(farmGateway.deleteCertification(certId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(certId))
                .verifyComplete();

        verify(farmGateway).deleteCertification(certId);
    }

    @Test
    void execute_propagatesError_whenGatewayFails() {
        when(farmGateway.deleteCertification(certId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.execute(certId))
                .verifyError(RuntimeException.class);
    }
}
