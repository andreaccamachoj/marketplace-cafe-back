package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
public final class ListProductsUseCase {

    private final ProductGateway productGateway;

    public record Command(
            String search,
            UUID categoryId,
            String region,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String certification,
            String roast,
            int page,
            int size,
            String sort
    ) {}

    public Flux<Product> execute(Command cmd) {
        return productGateway.findAll(
                cmd.search(), cmd.categoryId(), cmd.region(),
                cmd.minPrice(), cmd.maxPrice(),
                cmd.certification(), cmd.roast(),
                cmd.page(), cmd.size(), cmd.sort()
        );
    }
}
