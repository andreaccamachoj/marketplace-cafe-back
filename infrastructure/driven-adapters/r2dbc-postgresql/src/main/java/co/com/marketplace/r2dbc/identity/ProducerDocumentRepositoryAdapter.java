package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.ProducerDocument;
import co.com.marketplace.model.identity.gateways.ProducerDocumentGateway;
import co.com.marketplace.model.shared.DocStatus;
import co.com.marketplace.r2dbc.type.DocStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerDocumentRepositoryAdapter implements ProducerDocumentGateway {

    private final ProducerDocumentReactiveRepository repository;

    private static ProducerDocument toDomain(ProducerDocumentData d) {
        return ProducerDocument.builder()
                .id(d.getId())
                .producerId(d.getProducerId())
                .documentType(d.getDocumentType())
                .fileName(d.getFileName())
                .fileUrl(d.getFileUrl())
                .status(DocStatus.valueOf(d.getStatus().name()))
                .uploadedAt(d.getUploadedAt())
                .build();
    }

    private static ProducerDocumentData toData(ProducerDocument doc) {
        return ProducerDocumentData.builder()
                .id(doc.getId())
                .producerId(doc.getProducerId())
                .documentType(doc.getDocumentType())
                .fileName(doc.getFileName())
                .fileUrl(doc.getFileUrl())
                .status(DocStatusType.valueOf(doc.getStatus().name()))
                .uploadedAt(doc.getUploadedAt())
                .build();
    }

    @Override
    public Mono<ProducerDocument> save(ProducerDocument document) {
        return repository.save(toData(document))
                .doOnSubscribe(s -> log.debug("[ProducerDocumentRepositoryAdapter#save] DB request: producerId={}", document.getProducerId()))
                .doOnSuccess(r -> log.debug("[ProducerDocumentRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ProducerDocumentRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(ProducerDocumentRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<ProducerDocument> findByProducerId(UUID producerId) {
        return repository.findByProducerId(producerId)
                .doOnSubscribe(s -> log.debug("[ProducerDocumentRepositoryAdapter#findByProducerId] DB request: producerId={}", producerId))
                .doOnComplete(() -> log.debug("[ProducerDocumentRepositoryAdapter#findByProducerId] DB response: complete"))
                .doOnError(e -> log.error("[ProducerDocumentRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(ProducerDocumentRepositoryAdapter::toDomain);
    }
}
