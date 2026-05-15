package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class ProductRepositoryAdapterTest {

    @Mock private ProductReactiveRepository repository;
    @Mock private ProductImageReactiveRepository imageRepository;
    @Mock private ProductPresentationReactiveRepository presentationRepository;
    @Mock private ProductFlavorNoteReactiveRepository flavorNoteRepository;
    @Mock private ProductCuppingReactiveRepository cuppingRepository;
    @Mock private CertificationReactiveRepository certificationRepository;
    @Mock private R2dbcEntityTemplate template;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec rowsFetchSpec;
    @Mock private FetchSpec fSpec;

    @InjectMocks private ProductRepositoryAdapter adapter;

    private final UUID productId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private ProductData productData;

    @BeforeEach
    void setUp() {
        productData = ProductData.builder()
                .id(productId)
                .producerId(producerId)
                .name("Test Coffee")
                .description("Description")
                .price(new BigDecimal("15.00"))
                .status("active")
                .soldCount(5)
                .rating(4.5)
                .reviewCount(10)
                .stock(100)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private void stubDbSpec() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
    }

    private void stubFindById() {
        stubDbSpec();
        doReturn(Mono.just(productData)).when(rowsFetchSpec).one();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        when(imageRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(presentationRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(flavorNoteRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(cuppingRepository.findByProductId(productId)).thenReturn(Mono.empty());
    }

    @Test
    void findById_returnsProduct_whenFound() {
        stubFindById();

        StepVerifier.create(adapter.findById(productId))
                .expectNextMatches(p -> productId.equals(p.getId()) && "Test Coffee".equals(p.getName()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        stubDbSpec();
        doReturn(Mono.empty()).when(rowsFetchSpec).one();

        StepVerifier.create(adapter.findById(productId))
                .verifyComplete();
    }

    @Test
    void save_returnsProduct_whenSuccessful() {
        when(template.insert(any(ProductData.class))).thenReturn(Mono.just(productData));
        stubFindById();

        Product minimal = Product.builder()
                .id(productId)
                .producerId(producerId)
                .name("Test Coffee")
                .price(new BigDecimal("15.00"))
                .status(ProductStatus.active)
                .build();

        StepVerifier.create(adapter.save(minimal))
                .expectNextMatches(p -> productId.equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void updateStatus_completesSuccessfully() {
        when(template.update(any(Query.class), any(Update.class), eq(ProductData.class)))
                .thenReturn(Mono.just(1L));

        StepVerifier.create(adapter.updateStatus(productId, ProductStatus.inactive))
                .verifyComplete();
    }

    @Test
    void update_returnsProduct_whenSuccessful() {
        when(template.update(any(Query.class), any(Update.class), eq(ProductData.class)))
                .thenReturn(Mono.just(1L));
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.fetch()).thenReturn(fSpec);
        when(fSpec.rowsUpdated()).thenReturn(Mono.just(1L));
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(productData)).when(rowsFetchSpec).one();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        when(imageRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(presentationRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(flavorNoteRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(cuppingRepository.findByProductId(productId)).thenReturn(Mono.empty());

        Product updateProduct = Product.builder()
                .id(productId)
                .producerId(producerId)
                .name("Updated Coffee")
                .price(new BigDecimal("20.00"))
                .status(ProductStatus.active)
                .certificationCodes(null)
                .stock(10)
                .build();

        StepVerifier.create(adapter.update(updateProduct))
                .expectNextMatches(p -> productId.equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsProducts_withNoFilters() {
        stubDbSpec();
        doReturn(Flux.just(productData)).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, null, null, 0, 10, null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findAll_withAllFilters_executesQuery() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        UUID catId = UUID.randomUUID();
        StepVerifier.create(adapter.findAll(
                "coffee", catId, "Colombia",
                BigDecimal.ONE, BigDecimal.TEN,
                "organic", "medium",
                0, 5, "sold_count"))
                .verifyComplete();
    }

    @Test
    void findAll_withSortPriceAsc_executesQuery() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, null, null, 0, 10, "price_asc"))
                .verifyComplete();
    }

    @Test
    void findAll_withSortPriceDesc_executesQuery() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, null, null, 0, 10, "price_desc"))
                .verifyComplete();
    }

    @Test
    void countAll_returnsCount_withNoFilters() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(42L)).when(rowsFetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null, null, null, null, null))
                .expectNext(42L)
                .verifyComplete();
    }

    @Test
    void countAll_returnsCount_withFilters() {
        stubDbSpec();
        doReturn(Mono.just(5L)).when(rowsFetchSpec).one();

        UUID catId = UUID.randomUUID();
        StepVerifier.create(adapter.countAll(
                "coffee", catId, "Colombia",
                BigDecimal.ONE, BigDecimal.TEN,
                "organic", "medium"))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void findFeatured_returnsProducts() {
        stubDbSpec();
        doReturn(Flux.just(productData)).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findFeatured(5))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findFeatured_returnsEmpty_whenNone() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findFeatured(5))
                .verifyComplete();
    }

    @Test
    void findByProducerId_withNullStatus_returnsProducts() {
        stubDbSpec();
        doReturn(Flux.just(productData)).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findByProducerId_withStatus_returnsProducts() {
        stubDbSpec();
        doReturn(Flux.just(productData)).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, ProductStatus.active, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findAllForAdmin_returnsProducts() {
        stubDbSpec();
        doReturn(Flux.just(productData)).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAllForAdmin(0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void countByProducerId_withNullStatus_returnsCount() {
        when(repository.countByProducerIdAndStatus(producerId, null)).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.countByProducerId(producerId, null))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void countByProducerId_withStatus_returnsCount() {
        when(repository.countByProducerIdAndStatus(producerId, "active")).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countByProducerId(producerId, ProductStatus.active))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void toDomainShallow_withCertCodes_mapsCorrectly() {
        ProductData data = ProductData.builder()
                .id(productId)
                .producerId(producerId)
                .name("Coffee")
                .status("active")
                .certCodes("organic,fair_trade")
                .build();

        Product p = ProductRepositoryAdapter.toDomainShallow(data);

        assertThat(p.getCertificationCodes()).containsExactly("organic", "fair_trade");
    }

    @Test
    void toDomainShallow_withNullCertCodes_returnsEmptyList() {
        ProductData data = ProductData.builder()
                .id(productId)
                .status("active")
                .build();

        Product p = ProductRepositoryAdapter.toDomainShallow(data);

        assertThat(p.getCertificationCodes()).isEmpty();
    }

    @Test
    void toDomainShallow_withBlankCertCodes_returnsEmptyList() {
        ProductData data = ProductData.builder()
                .id(productId)
                .status("active")
                .certCodes("  ")
                .build();

        Product p = ProductRepositoryAdapter.toDomainShallow(data);

        assertThat(p.getCertificationCodes()).isEmpty();
    }

    @Test
    void toDomain_mapsAllFields() {
        Product p = ProductRepositoryAdapter.toDomain(
                productData,
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(1, 2),
                List.of("organic"));

        assertThat(p.getId()).isEqualTo(productId);
        assertThat(p.getName()).isEqualTo("Test Coffee");
        assertThat(p.getStatus()).isEqualTo(ProductStatus.active);
        assertThat(p.getRoastLevelIds()).containsExactly(1, 2);
        assertThat(p.getCertificationCodes()).containsExactly("organic");
    }

    @Test
    void toData_mapsProductFields() {
        Product p = Product.builder()
                .id(productId)
                .producerId(producerId)
                .name("Test Coffee")
                .price(new BigDecimal("15.00"))
                .status(ProductStatus.active)
                .soldCount(5)
                .build();

        ProductData data = ProductRepositoryAdapter.toData(p);

        assertThat(data.getId()).isEqualTo(productId);
        assertThat(data.getName()).isEqualTo("Test Coffee");
        assertThat(data.getStatus()).isEqualTo("active");
    }

    @Test
    void toData_withNullStatus_setsNullStatus() {
        Product p = Product.builder()
                .id(productId)
                .name("Coffee")
                .build();

        ProductData data = ProductRepositoryAdapter.toData(p);

        assertThat(data.getStatus()).isNull();
    }
}
