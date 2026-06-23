package co.com.marketplace.model.catalog.gateways;

import reactor.core.publisher.Mono;

public interface ImageStorageGateway {
    Mono<String> upload(byte[] content, String contentType, String key);
    Mono<Void> delete(String key);
}
