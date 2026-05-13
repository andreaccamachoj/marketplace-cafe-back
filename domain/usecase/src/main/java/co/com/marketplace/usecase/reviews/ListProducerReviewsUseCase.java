package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ListProducerReviewsUseCase {

    private final ReviewGateway reviewGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public Flux<Review> execute(UUID userId, int page, int size) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND", "Producer profile not found")))
                .flatMapMany(profile -> reviewGateway.findByProducerId(profile.getId(), page, size));
    }
}
