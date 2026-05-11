package co.com.marketplace.api.reviews;

import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import co.com.marketplace.usecase.reviews.CreateReviewUseCase;
import co.com.marketplace.usecase.reviews.ModerateReviewUseCase;
import co.com.marketplace.usecase.reviews.ReplyReviewUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReviewHandler {

    private final CreateReviewUseCase createReviewUseCase;
    private final ReplyReviewUseCase replyReviewUseCase;
    private final ModerateReviewUseCase moderateReviewUseCase;
    private final ReviewGateway reviewGateway;
    private final TransactionalOperator tx;

    record CreateReviewRequest(UUID productId, UUID orderId, short rating, String title, String body) {}
    record ReplyRequest(String body) {}
    record ModerateRequest(String action) {}

    public Mono<ServerResponse> create(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(CreateReviewRequest.class)
                        .flatMap(body -> createReviewUseCase.execute(
                                new CreateReviewUseCase.Command(body.productId(), body.orderId(), uid,
                                        body.rating(), body.title(), body.body()))
                                .as(tx::transactional)))
                .flatMap(review -> ServerResponse.status(HttpStatus.CREATED).bodyValue(review));
    }

    public Mono<ServerResponse> reply(ServerRequest request) {
        UUID reviewId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ReplyRequest.class)
                        .flatMap(body -> replyReviewUseCase.execute(reviewId, uid, body.body())
                                .as(tx::transactional)))
                .flatMap(reply -> ServerResponse.status(HttpStatus.CREATED).bodyValue(reply));
    }

    public Mono<ServerResponse> moderate(ServerRequest request) {
        UUID reviewId = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(ModerateRequest.class)
                .flatMap(body -> moderateReviewUseCase.execute(reviewId, ReviewStatus.valueOf(body.action())))
                .flatMap(review -> ServerResponse.ok().bodyValue(review));
    }

    public Mono<ServerResponse> listMyReviews(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(50);
        return userId(request)
                .flatMapMany(uid -> reviewGateway.findByBuyerId(uid, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
