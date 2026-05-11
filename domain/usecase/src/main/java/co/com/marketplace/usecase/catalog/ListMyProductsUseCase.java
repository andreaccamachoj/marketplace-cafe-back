package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class ListMyProductsUseCase {

    private final ProductGateway productGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public ListMyProductsUseCase(ProductGateway productGateway, ProducerProfileGateway producerProfileGateway) {
        this.productGateway = productGateway;
        this.producerProfileGateway = producerProfileGateway;
    }

    public Flux<Product> execute(UUID userId, ProductStatus status, int page, int size) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMapMany(profile -> productGateway.findByProducerId(profile.getId(), status, page, size));
    }
}
