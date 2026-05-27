package co.com.marketplace.r2dbc.reviews;

import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.r2dbc.type.ReviewStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class ReviewRepositoryAdapterTest {

    @Mock private ReviewReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private ReviewRepositoryAdapter adapter;

    private final UUID reviewId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private ReviewData reviewData;

    @BeforeEach
    void setUp() {
        reviewData = ReviewData.builder()
                .id(reviewId)
                .productId(productId)
                .buyerId(buyerId)
                .rating((short) 5)
                .title("Excelente")
                .body("Muy buen café")
                .status(ReviewStatusType.published)
                .isVerifiedPurchase(true)
                .helpfulCount(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findById_returnsReview_whenFound() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(reviewData)).when(fetchSpec).one();

        StepVerifier.create(adapter.findById(reviewId))
                .expectNextMatches(r -> reviewId.equals(r.getId()) && ReviewStatus.published.equals(r.getStatus()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.empty()).when(fetchSpec).one();

        StepVerifier.create(adapter.findById(reviewId))
                .verifyComplete();
    }

    @Test
    void countByProductId_returnsCount() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(3L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countByProductId(productId))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void countByProducerId_returnsCount() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(7L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countByProducerId(producerId))
                .expectNext(7L)
                .verifyComplete();
    }

    @Test
    void existsByBuyerAndProduct_returnsTrue_whenExists() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(true)).when(fetchSpec).one();

        StepVerifier.create(adapter.existsByBuyerAndProduct(buyerId, productId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByBuyerAndProduct_returnsFalse_whenNotExists() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(false)).when(fetchSpec).one();

        StepVerifier.create(adapter.existsByBuyerAndProduct(buyerId, productId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByBuyerId_returnsReviews_whenFound() {
        when(repo.findByBuyerId(buyerId, 10, 0L)).thenReturn(Flux.just(reviewData));

        StepVerifier.create(adapter.findByBuyerId(buyerId, 0, 10))
                .expectNextMatches(r -> buyerId.equals(r.getBuyerId()))
                .verifyComplete();
    }

    @Test
    void findByBuyerId_returnsEmpty_whenNone() {
        when(repo.findByBuyerId(buyerId, 10, 0L)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByBuyerId(buyerId, 0, 10))
                .verifyComplete();
    }

    @Test
    void findByProductId_usesDatabaseClient() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProductId(productId, 0, 10))
                .verifyComplete();
    }

    @Test
    void findByProducerId_usesDatabaseClient() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, 0, 10))
                .verifyComplete();
    }

    @Test
    void findById_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.findById(reviewId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProductId_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.error(new RuntimeException("DB error"))).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProductId(productId, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.error(new RuntimeException("DB error"))).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByBuyerId_propagatesError() {
        when(repo.findByBuyerId(buyerId, 10, 0L)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByBuyerId(buyerId, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void existsByBuyerAndProduct_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.existsByBuyerAndProduct(buyerId, productId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countByProductId_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.countByProductId(productId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countByProducerId_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.countByProducerId(producerId))
                .verifyError(RuntimeException.class);
    }
}
