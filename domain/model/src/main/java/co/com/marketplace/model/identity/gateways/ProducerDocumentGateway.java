package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.ProducerDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProducerDocumentGateway {
    Mono<ProducerDocument> save(ProducerDocument document);
    Flux<ProducerDocument> findByProducerId(UUID producerId);
}
