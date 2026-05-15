package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Certification;
import co.com.marketplace.model.catalog.gateways.CertificationGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCertificationsUseCaseTest {

    @Mock private CertificationGateway certificationGateway;

    @InjectMocks
    private ListCertificationsUseCase useCase;

    @Test
    void execute_returnsCertifications() {
        Certification c = Certification.builder().id(1).code("ORGANIC").name("Organic").build();
        when(certificationGateway.findAll()).thenReturn(Flux.just(c));

        StepVerifier.create(useCase.execute())
                .expectNextMatches(cert -> "ORGANIC".equals(cert.getCode()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNone() {
        when(certificationGateway.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }
}
