package co.com.marketplace.r2dbc.farm;

import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.shared.DocStatus;
import co.com.marketplace.r2dbc.type.DocStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmRepositoryAdapterTest {

    @Mock private FarmReactiveRepository farmRepository;
    @Mock private FarmCertificationReactiveRepository certRepository;
    @Mock private R2dbcEntityTemplate template;

    @InjectMocks private FarmRepositoryAdapter adapter;

    private final UUID farmId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private final UUID certId = UUID.randomUUID();
    private FarmData farmData;
    private Farm farm;
    private FarmCertificationData certData;

    @BeforeEach
    void setUp() {
        farmData = FarmData.builder()
                .id(farmId)
                .producerId(producerId)
                .name("Finca La Esperanza")
                .municipality("Salamina")
                .department("Caldas")
                .altitudeMasl(BigDecimal.valueOf(1800))
                .areaHectares(BigDecimal.valueOf(5.5))
                .mainVariety("Castillo")
                .process("washed")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        farm = Farm.builder()
                .id(farmId)
                .producerId(producerId)
                .name("Finca La Esperanza")
                .municipality("Salamina")
                .department("Caldas")
                .altitudeMasl(BigDecimal.valueOf(1800))
                .areaHectares(BigDecimal.valueOf(5.5))
                .mainVariety("Castillo")
                .process("washed")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        certData = FarmCertificationData.builder()
                .id(certId)
                .farmId(farmId)
                .certificationId(1)
                .issuer("USDA")
                .status(DocStatusType.approved)
                .notes("organic|Orgánico")
                .build();
    }

    @Test
    void save_returnsFarm_whenSuccessful() {
        when(farmRepository.save(any(FarmData.class))).thenReturn(Mono.just(farmData));

        StepVerifier.create(adapter.save(farm))
                .expectNextMatches(f -> farmId.equals(f.getId()) && "Finca La Esperanza".equals(f.getName()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(farmRepository.save(any(FarmData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(farm))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_returnsFarm_whenFound() {
        when(farmRepository.findByProducerId(producerId)).thenReturn(Mono.just(farmData));

        StepVerifier.create(adapter.findByProducerId(producerId))
                .expectNextMatches(f -> producerId.equals(f.getProducerId()))
                .verifyComplete();
    }

    @Test
    void findByProducerId_returnsEmpty_whenNotFound() {
        when(farmRepository.findByProducerId(producerId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyComplete();
    }

    @Test
    void update_returnsFarm_whenSuccessful() {
        when(template.update(any(Query.class), any(Update.class), eq(FarmData.class))).thenReturn(Mono.just(1L));
        when(farmRepository.findById(farmId)).thenReturn(Mono.just(farmData));

        StepVerifier.create(adapter.update(farm))
                .expectNextMatches(f -> farmId.equals(f.getId()))
                .verifyComplete();
    }

    @Test
    void saveCertification_returnsCert_whenSuccessful() {
        when(certRepository.save(any(FarmCertificationData.class))).thenReturn(Mono.just(certData));

        FarmCertification cert = FarmCertification.builder()
                .id(certId)
                .farmId(farmId)
                .certificationId(1)
                .issuer("USDA")
                .status(DocStatus.approved)
                .notes("organic|Orgánico")
                .build();

        StepVerifier.create(adapter.saveCertification(cert))
                .expectNextMatches(c -> certId.equals(c.getId()))
                .verifyComplete();
    }

    @Test
    void deleteCertification_completesSuccessfully() {
        when(certRepository.deleteById(certId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteCertification(certId))
                .verifyComplete();
    }

    @Test
    void findCertificationsByFarmId_returnsList_whenFound() {
        when(certRepository.findByFarmId(farmId)).thenReturn(Flux.just(certData));

        StepVerifier.create(adapter.findCertificationsByFarmId(farmId))
                .expectNextMatches(c -> farmId.equals(c.getFarmId()))
                .verifyComplete();
    }

    @Test
    void findCertificationsByFarmId_returnsEmpty_whenNone() {
        when(certRepository.findByFarmId(farmId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findCertificationsByFarmId(farmId))
                .verifyComplete();
    }

    @Test
    void findByProducerId_propagatesError() {
        when(farmRepository.findByProducerId(producerId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void update_propagatesError() {
        when(template.update(any(Query.class), any(Update.class), eq(FarmData.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));
        when(farmRepository.findById(farmId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.update(farm))
                .verifyError(RuntimeException.class);
    }

    @Test
    void saveCertification_propagatesError() {
        when(certRepository.save(any(FarmCertificationData.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        FarmCertification cert = FarmCertification.builder()
                .id(certId).farmId(farmId).certificationId(1)
                .issuer("USDA").status(DocStatus.approved).notes("organic|Orgánico").build();

        StepVerifier.create(adapter.saveCertification(cert))
                .verifyError(RuntimeException.class);
    }

    @Test
    void deleteCertification_propagatesError() {
        when(certRepository.deleteById(certId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.deleteCertification(certId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findCertificationsByFarmId_propagatesError() {
        when(certRepository.findByFarmId(farmId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findCertificationsByFarmId(farmId))
                .verifyError(RuntimeException.class);
    }
}
