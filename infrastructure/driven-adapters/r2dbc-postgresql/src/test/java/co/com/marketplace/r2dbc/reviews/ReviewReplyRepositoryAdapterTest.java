package co.com.marketplace.r2dbc.reviews;

import co.com.marketplace.model.reviews.ReviewReply;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class ReviewReplyRepositoryAdapterTest {

    @Mock private ReviewReplyReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private ReviewReplyRepositoryAdapter adapter;

    private final UUID replyId = UUID.randomUUID();
    private final UUID reviewId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private ReviewReplyData replyData;
    private ReviewReply reply;

    @BeforeEach
    void setUp() {
        replyData = ReviewReplyData.builder()
                .id(replyId)
                .reviewId(reviewId)
                .producerId(producerId)
                .body("Gracias por tu reseña")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        reply = ReviewReply.builder()
                .id(replyId)
                .reviewId(reviewId)
                .producerId(producerId)
                .body("Gracias por tu reseña")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsReply_whenSuccessful() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(reply)).when(fetchSpec).one();

        StepVerifier.create(adapter.save(reply))
                .expectNextMatches(r -> reviewId.equals(r.getReviewId()) && "Gracias por tu reseña".equals(r.getBody()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenDatabaseFails() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.save(reply))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByReviewId_returnsReply_whenFound() {
        when(repo.findByReviewId(reviewId)).thenReturn(Mono.just(replyData));

        StepVerifier.create(adapter.findByReviewId(reviewId))
                .expectNextMatches(r -> reviewId.equals(r.getReviewId()))
                .verifyComplete();
    }

    @Test
    void findByReviewId_returnsEmpty_whenNotFound() {
        when(repo.findByReviewId(reviewId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByReviewId(reviewId))
                .verifyComplete();
    }

    @Test
    void findByReviewId_propagatesError() {
        when(repo.findByReviewId(reviewId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByReviewId(reviewId))
                .verifyError(RuntimeException.class);
    }
}
