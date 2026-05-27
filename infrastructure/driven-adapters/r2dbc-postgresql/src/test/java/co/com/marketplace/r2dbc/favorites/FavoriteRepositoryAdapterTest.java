package co.com.marketplace.r2dbc.favorites;

import co.com.marketplace.model.favorites.Favorite;
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
class FavoriteRepositoryAdapterTest {

    @Mock private FavoriteReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private FavoriteRepositoryAdapter adapter;

    private final UUID favoriteId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private FavoriteData favoriteData;
    private Favorite favorite;

    @BeforeEach
    void setUp() {
        favoriteData = FavoriteData.builder()
                .id(favoriteId)
                .userId(userId)
                .productId(productId)
                .addedAt(OffsetDateTime.now())
                .build();

        favorite = Favorite.builder()
                .id(favoriteId)
                .userId(userId)
                .productId(productId)
                .addedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findByUserId_returnsFavorites_whenFound() {
        when(repo.findByUserId(userId)).thenReturn(Flux.just(favoriteData));

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNextMatches(f -> userId.equals(f.getUserId()) && productId.equals(f.getProductId()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNone() {
        when(repo.findByUserId(userId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyComplete();
    }

    @Test
    void save_returnsFavorite_whenSuccessful() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.save(favorite))
                .expectNextMatches(f -> favoriteId.equals(f.getId()) && userId.equals(f.getUserId()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenDatabaseFails() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(favorite))
                .verifyError(RuntimeException.class);
    }

    @Test
    void delete_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.delete(userId, productId))
                .verifyComplete();
    }

    @Test
    void exists_returnsTrue_whenFound() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(true)).when(fetchSpec).one();

        StepVerifier.create(adapter.exists(userId, productId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void exists_returnsFalse_whenNotFound() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(false)).when(fetchSpec).one();

        StepVerifier.create(adapter.exists(userId, productId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void findByUserId_propagatesError() {
        when(repo.findByUserId(userId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void delete_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.delete(userId, productId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void exists_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.exists(userId, productId))
                .verifyError(RuntimeException.class);
    }
}
