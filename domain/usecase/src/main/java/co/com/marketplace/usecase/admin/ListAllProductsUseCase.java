package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public final class ListAllProductsUseCase {
    private final ProductGateway productGateway;

    public Flux<Product> execute(int page, int size) {
        return productGateway.findAllForAdmin(page, size);
    }
}
