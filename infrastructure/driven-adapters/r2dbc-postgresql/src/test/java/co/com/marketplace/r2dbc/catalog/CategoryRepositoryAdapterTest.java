package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryAdapterTest {

    @Mock private CategoryReactiveRepository repository;
    @Mock private R2dbcEntityTemplate template;

    @InjectMocks private CategoryRepositoryAdapter adapter;

    private final UUID categoryId = UUID.randomUUID();
    private CategoryData categoryData;
    private Category category;

    @BeforeEach
    void setUp() {
        categoryData = CategoryData.builder()
                .id(categoryId)
                .name("Café en Grano")
                .slug("cafe-en-grano")
                .description("Café sin moler")
                .isActive(true)
                .iconEmoji("☕")
                .createdAt(OffsetDateTime.now())
                .build();

        category = Category.builder()
                .id(categoryId)
                .name("Café en Grano")
                .slug("cafe-en-grano")
                .description("Café sin moler")
                .isActive(true)
                .iconEmoji("☕")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findAll_returnsCategories_whenFound() {
        when(repository.findAll()).thenReturn(Flux.just(categoryData));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(c -> categoryId.equals(c.getId()) && "Café en Grano".equals(c.getName()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNone() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findById_returnsCategory_whenFound() {
        when(repository.findById(categoryId)).thenReturn(Mono.just(categoryData));

        StepVerifier.create(adapter.findById(categoryId))
                .expectNextMatches(c -> categoryId.equals(c.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repository.findById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(categoryId))
                .verifyComplete();
    }

    @Test
    void findBySlug_returnsCategory_whenFound() {
        when(repository.findBySlug("cafe-en-grano")).thenReturn(Mono.just(categoryData));

        StepVerifier.create(adapter.findBySlug("cafe-en-grano"))
                .expectNextMatches(c -> "cafe-en-grano".equals(c.getSlug()))
                .verifyComplete();
    }

    @Test
    void findBySlug_returnsEmpty_whenNotFound() {
        when(repository.findBySlug("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findBySlug("unknown"))
                .verifyComplete();
    }

    @Test
    void save_returnsCategory_whenSuccessful() {
        when(repository.save(any(CategoryData.class))).thenReturn(Mono.just(categoryData));

        StepVerifier.create(adapter.save(category))
                .expectNextMatches(c -> "Café en Grano".equals(c.getName()))
                .verifyComplete();
    }

    @Test
    void update_returnsUpdatedCategory_whenSuccessful() {
        when(template.update(any(Query.class), any(Update.class), eq(CategoryData.class)))
                .thenReturn(Mono.just(1L));
        when(repository.findById(categoryId)).thenReturn(Mono.just(categoryData));

        StepVerifier.create(adapter.update(category))
                .expectNextMatches(c -> categoryId.equals(c.getId()))
                .verifyComplete();
    }

    @Test
    void deleteById_completesSuccessfully() {
        when(repository.deleteById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(categoryId))
                .verifyComplete();
    }
}
