package co.com.marketplace.model.catalog.gateways;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductGateway {
    Mono<Product> save(Product product);
    Mono<Product> findById(UUID id);
    Mono<Product> update(Product product);
    Mono<Void> updateStatus(UUID id, ProductStatus status);
    Flux<Product> findAll(String search, UUID categoryId, String region,
                          BigDecimal minPrice, BigDecimal maxPrice,
                          String certification, String roast,
                          int page, int size, String sort);
    Mono<Long> countAll(String search, UUID categoryId, String region,
                        BigDecimal minPrice, BigDecimal maxPrice,
                        String certification, String roast);
    Flux<Product> findFeatured(int limit);
    Flux<Product> findByProducerId(UUID producerId, ProductStatus status, int page, int size);
    Mono<Long> countByProducerId(UUID producerId, ProductStatus status);
}
