package co.com.marketplace.r2dbc.reviews;

import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import co.com.marketplace.r2dbc.type.ReviewStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewGateway {

    private final ReviewReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<Review> save(Review review) {
        UUID id = review.getId() != null ? review.getId() : java.util.UUID.randomUUID();
        var spec = db.sql(
                "INSERT INTO marketplace.reviews " +
                "(id, product_id, buyer_id, order_id, rating, title, body, status, " +
                " is_verified_purchase, helpful_count, created_at, updated_at) " +
                "VALUES (:id, :productId, :buyerId, :orderId, :rating, :title, :body, " +
                " CAST(:status AS marketplace.review_status), :isVerifiedPurchase, :helpfulCount, NOW(), NOW()) " +
                "RETURNING *")
                .bind("id", id)
                .bind("productId", review.getProductId())
                .bind("buyerId", review.getBuyerId())
                .bind("rating", review.getRating())
                .bind("status", review.getStatus().name())
                .bind("isVerifiedPurchase", review.isVerifiedPurchase())
                .bind("helpfulCount", review.getHelpfulCount());
        spec = review.getOrderId() != null
                ? spec.bind("orderId", review.getOrderId())
                : spec.bindNull("orderId", UUID.class);
        spec = review.getTitle() != null
                ? spec.bind("title", review.getTitle())
                : spec.bindNull("title", String.class);
        spec = review.getBody() != null
                ? spec.bind("body", review.getBody())
                : spec.bindNull("body", String.class);
        return spec
                .map((row, meta) -> ReviewData.builder()
                        .id(row.get("id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .buyerId(row.get("buyer_id", UUID.class))
                        .orderId(row.get("order_id", UUID.class))
                        .rating(row.get("rating", Short.class))
                        .title(row.get("title", String.class))
                        .body(row.get("body", String.class))
                        .status(ReviewStatusType.valueOf(row.get("status", String.class)))
                        .isVerifiedPurchase(Boolean.TRUE.equals(row.get("is_verified_purchase", Boolean.class)))
                        .helpfulCount(row.get("helpful_count", Integer.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .updatedAt(row.get("updated_at", OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#save] DB request: productId={}, buyerId={}", review.getProductId(), review.getBuyerId()))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#save] DB response: id={}", r != null ? r.getId() : null))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#save] DB error: {} {}", e.getClass().getSimpleName(), e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Review> findById(UUID id) {
        return repo.findById(id)
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Review> updateStatus(UUID id, ReviewStatus status) {
        return db.sql("UPDATE marketplace.reviews SET status = :status, updated_at = NOW() WHERE id = :id RETURNING *")
                .bind("status", ReviewStatusType.valueOf(status.name()))
                .bind("id", id)
                .map((row, meta) -> ReviewData.builder()
                        .id(row.get("id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .buyerId(row.get("buyer_id", UUID.class))
                        .orderId(row.get("order_id", UUID.class))
                        .rating(row.get("rating", Short.class))
                        .title(row.get("title", String.class))
                        .body(row.get("body", String.class))
                        .status(row.get("status", ReviewStatusType.class))
                        .isVerifiedPurchase(Boolean.TRUE.equals(row.get("is_verified_purchase", Boolean.class)))
                        .helpfulCount(row.get("helpful_count", Integer.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .updatedAt(row.get("updated_at", OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#updateStatus] DB request: id={}, status={}", id, status))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#updateStatus] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#updateStatus] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<Review> findByProductId(UUID productId, int page, int size) {
        return repo.findByProductId(productId, size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#findByProductId] DB request: productId={}, page={}, size={}", productId, page, size))
                .doOnComplete(() -> log.debug("[ReviewRepositoryAdapter#findByProductId] DB response: complete"))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#findByProductId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByProductId(UUID productId) {
        return db.sql("SELECT COUNT(*) FROM marketplace.reviews WHERE product_id = :productId")
                .bind("productId", productId)
                .map((row, meta) -> row.get(0, Long.class))
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#countByProductId] DB request: productId={}", productId))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#countByProductId] DB response: result={}", r))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#countByProductId] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<Review> findByProducerId(UUID producerId, int page, int size) {
        return db.sql(
                "SELECT r.id, r.product_id, r.buyer_id, r.order_id, r.rating, r.title, r.body, " +
                "       r.status, r.is_verified_purchase, r.helpful_count, r.created_at, r.updated_at, " +
                "       u.full_name AS buyer_name, " +
                "       p.name AS product_name, p.emoji AS product_emoji, " +
                "       rr.body AS producer_reply, rr.created_at AS producer_reply_date " +
                "FROM marketplace.reviews r " +
                "JOIN marketplace.products p ON p.id = r.product_id " +
                "JOIN marketplace.users u ON u.id = r.buyer_id " +
                "LEFT JOIN marketplace.review_replies rr ON rr.review_id = r.id " +
                "WHERE p.producer_id = :producerId " +
                "ORDER BY r.created_at DESC LIMIT :size OFFSET :offset")
                .bind("producerId", producerId)
                .bind("size", size)
                .bind("offset", (long) page * size)
                .map((row, meta) -> {
                    String fullName = row.get("buyer_name", String.class);
                    String initials = fullName != null
                            ? java.util.Arrays.stream(fullName.trim().split("\\s+"))
                                    .filter(w -> !w.isEmpty())
                                    .map(w -> String.valueOf(w.charAt(0)).toUpperCase())
                                    .limit(2)
                                    .reduce("", String::concat)
                            : "C";
                    return Review.builder()
                            .id(row.get("id", UUID.class))
                            .productId(row.get("product_id", UUID.class))
                            .buyerId(row.get("buyer_id", UUID.class))
                            .orderId(row.get("order_id", UUID.class))
                            .rating(row.get("rating", Short.class))
                            .title(row.get("title", String.class))
                            .body(row.get("body", String.class))
                            .status(co.com.marketplace.model.reviews.ReviewStatus.valueOf(
                                    ReviewStatusType.valueOf(row.get("status", String.class)).name()))
                            .isVerifiedPurchase(Boolean.TRUE.equals(row.get("is_verified_purchase", Boolean.class)))
                            .helpfulCount(row.get("helpful_count", Integer.class))
                            .createdAt(row.get("created_at", OffsetDateTime.class))
                            .updatedAt(row.get("updated_at", OffsetDateTime.class))
                            .buyerName(fullName != null ? fullName : "Comprador")
                            .buyerInitials(initials.isEmpty() ? "C" : initials)
                            .productName(row.get("product_name", String.class))
                            .productEmoji(row.get("product_emoji", String.class))
                            .producerReply(row.get("producer_reply", String.class))
                            .producerReplyDate(row.get("producer_reply_date", OffsetDateTime.class))
                            .build();
                })
                .all()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#findByProducerId] DB request: producerId={}", producerId))
                .doOnComplete(() -> log.debug("[ReviewRepositoryAdapter#findByProducerId] DB response: complete"))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Long> countByProducerId(UUID producerId) {
        return db.sql("SELECT COUNT(r.id) FROM marketplace.reviews r " +
                      "JOIN marketplace.products p ON p.id = r.product_id " +
                      "WHERE p.producer_id = :producerId")
                .bind("producerId", producerId)
                .map((row, meta) -> row.get(0, Long.class))
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#countByProducerId] DB request: producerId={}", producerId))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#countByProducerId] DB response: result={}", r))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#countByProducerId] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Boolean> existsByBuyerAndProduct(UUID buyerId, UUID productId) {
        return db.sql("SELECT EXISTS(SELECT 1 FROM marketplace.reviews WHERE buyer_id = :buyerId AND product_id = :productId)")
                .bind("buyerId", buyerId)
                .bind("productId", productId)
                .map((row, meta) -> row.get(0, Boolean.class))
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#existsByBuyerAndProduct] DB request: buyerId={}, productId={}", buyerId, productId))
                .doOnSuccess(r -> log.debug("[ReviewRepositoryAdapter#existsByBuyerAndProduct] DB response: result={}", r))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#existsByBuyerAndProduct] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<Review> findByBuyerId(UUID buyerId, int page, int size) {
        return repo.findByBuyerId(buyerId, size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[ReviewRepositoryAdapter#findByBuyerId] DB request: buyerId={}, page={}, size={}", buyerId, page, size))
                .doOnComplete(() -> log.debug("[ReviewRepositoryAdapter#findByBuyerId] DB response: complete"))
                .doOnError(e -> log.error("[ReviewRepositoryAdapter#findByBuyerId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private Review toDomain(ReviewData d) {
        return Review.builder()
                .id(d.getId())
                .productId(d.getProductId())
                .buyerId(d.getBuyerId())
                .orderId(d.getOrderId())
                .rating(d.getRating())
                .title(d.getTitle())
                .body(d.getBody())
                .status(ReviewStatus.valueOf(d.getStatus().name()))
                .isVerifiedPurchase(d.isVerifiedPurchase())
                .helpfulCount(d.getHelpfulCount())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private ReviewData toData(Review r) {
        return ReviewData.builder()
                .id(r.getId())
                .productId(r.getProductId())
                .buyerId(r.getBuyerId())
                .orderId(r.getOrderId())
                .rating(r.getRating())
                .title(r.getTitle())
                .body(r.getBody())
                .status(ReviewStatusType.valueOf(r.getStatus().name()))
                .isVerifiedPurchase(r.isVerifiedPurchase())
                .helpfulCount(r.getHelpfulCount())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
