package co.com.marketplace.r2dbc.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificationRepositoryAdapterTest {

    @Mock private CertificationReactiveRepository repository;

    @InjectMocks private CertificationRepositoryAdapter adapter;

    private CertificationData certData;

    @BeforeEach
    void setUp() {
        certData = CertificationData.builder()
                .id(1)
                .code("ORGANIC")
                .name("Orgánico")
                .issuingBody("USDA")
                .description("Certificación orgánica")
                .build();
    }

    @Test
    void findAll_returnsCertifications_whenFound() {
        when(repository.findAll()).thenReturn(Flux.just(certData));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(c -> "ORGANIC".equals(c.getCode()) && "Orgánico".equals(c.getName()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNone() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findByCode_returnsCertification_whenFound() {
        when(repository.findByCode("ORGANIC")).thenReturn(Mono.just(certData));

        StepVerifier.create(adapter.findByCode("ORGANIC"))
                .expectNextMatches(c -> "ORGANIC".equals(c.getCode()))
                .verifyComplete();
    }

    @Test
    void findByCode_returnsEmpty_whenNotFound() {
        when(repository.findByCode("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode("UNKNOWN"))
                .verifyComplete();
    }
}
