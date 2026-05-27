package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.ProducerDocument;
import co.com.marketplace.model.shared.DocStatus;
import co.com.marketplace.r2dbc.type.DocStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProducerDocumentRepositoryAdapterTest {

    @Mock private ProducerDocumentReactiveRepository repository;

    @InjectMocks private ProducerDocumentRepositoryAdapter adapter;

    private final UUID docId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private ProducerDocumentData docData;
    private ProducerDocument doc;

    @BeforeEach
    void setUp() {
        docData = ProducerDocumentData.builder()
                .id(docId)
                .producerId(producerId)
                .documentType("RUT")
                .fileName("rut.pdf")
                .fileUrl("https://storage/rut.pdf")
                .status(DocStatusType.pending)
                .uploadedAt(OffsetDateTime.now())
                .build();

        doc = ProducerDocument.builder()
                .id(docId)
                .producerId(producerId)
                .documentType("RUT")
                .fileName("rut.pdf")
                .fileUrl("https://storage/rut.pdf")
                .status(DocStatus.pending)
                .uploadedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsDocument_whenSuccessful() {
        when(repository.save(any(ProducerDocumentData.class))).thenReturn(Mono.just(docData));

        StepVerifier.create(adapter.save(doc))
                .expectNextMatches(d -> docId.equals(d.getId()) && "RUT".equals(d.getDocumentType()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repository.save(any(ProducerDocumentData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(doc))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_returnsDocuments_whenFound() {
        when(repository.findByProducerId(producerId)).thenReturn(Flux.just(docData));

        StepVerifier.create(adapter.findByProducerId(producerId))
                .expectNextMatches(d -> producerId.equals(d.getProducerId()))
                .verifyComplete();
    }

    @Test
    void findByProducerId_returnsEmpty_whenNone() {
        when(repository.findByProducerId(producerId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyComplete();
    }

    @Test
    void findByProducerId_propagatesError() {
        when(repository.findByProducerId(producerId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyError(RuntimeException.class);
    }
}
