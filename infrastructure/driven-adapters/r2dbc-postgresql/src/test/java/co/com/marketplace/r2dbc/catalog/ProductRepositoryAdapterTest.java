package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductCupping;
import co.com.marketplace.model.catalog.ProductFlavorNote;
import co.com.marketplace.model.catalog.ProductImage;
import co.com.marketplace.model.catalog.ProductPresentation;
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
import static org.mockito.Mockito.verify;
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

    @Test
    void save_propagatesError() {
        when(template.insert(any(ProductData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        Product minimal = Product.builder()
                .id(productId)
                .producerId(producerId)
                .name("Test Coffee")
                .price(new BigDecimal("15.00"))
                .status(ProductStatus.active)
                .build();

        StepVerifier.create(adapter.save(minimal))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findById_propagatesError() {
        stubDbSpec();
        doReturn(Mono.error(new RuntimeException("DB error"))).when(rowsFetchSpec).one();

        StepVerifier.create(adapter.findById(productId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void update_propagatesError() {
        when(template.update(any(Query.class), any(Update.class), eq(ProductData.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.fetch()).thenReturn(fSpec);
        when(fSpec.rowsUpdated()).thenReturn(Mono.just(1L));
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.empty()).when(rowsFetchSpec).one();

        Product updateProduct = Product.builder()
                .id(productId)
                .producerId(producerId)
                .name("Updated Coffee")
                .price(new BigDecimal("20.00"))
                .status(ProductStatus.active)
                .build();

        StepVerifier.create(adapter.update(updateProduct))
                .verifyError(RuntimeException.class);
    }

    @Test
    void updateStatus_propagatesError() {
        when(template.update(any(Query.class), any(Update.class), eq(ProductData.class)))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.updateStatus(productId, ProductStatus.inactive))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findAll_propagatesError() {
        stubDbSpec();
        doReturn(Flux.error(new RuntimeException("DB error"))).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, null, null, 0, 10, null))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countAll_propagatesError() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(rowsFetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null, null, null, null, null))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findFeatured_propagatesError() {
        stubDbSpec();
        doReturn(Flux.error(new RuntimeException("DB error"))).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findFeatured(5))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_propagatesError() {
        stubDbSpec();
        doReturn(Flux.error(new RuntimeException("DB error"))).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, null, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findAllForAdmin_propagatesError() {
        stubDbSpec();
        doReturn(Flux.error(new RuntimeException("DB error"))).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAllForAdmin(0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countByProducerId_propagatesError() {
        when(repository.countByProducerIdAndStatus(producerId, null))
                .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.countByProducerId(producerId, null))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findAll_withOnlySearch_bindsSearch() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll("coffee", null, null, null, null, null, null, 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("search"), eq("%coffee%"));
    }

    @Test
    void findAll_withOnlyCategoryId_bindsCategoryId() {
        UUID catId = UUID.randomUUID();
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, catId, null, null, null, null, null, 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("categoryId"), eq(catId));
    }

    @Test
    void findAll_withOnlyRegion_bindsRegion() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, "Huila", null, null, null, null, 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("region"), eq("Huila"));
    }

    @Test
    void findAll_withOnlyMinMaxPrice_bindsMinMaxPrice() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, BigDecimal.ONE, BigDecimal.TEN, null, null, 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("minPrice"), eq(BigDecimal.ONE));
        verify(spec).bind(eq("maxPrice"), eq(BigDecimal.TEN));
    }

    @Test
    void findAll_withOnlyCertification_addsCertJoin() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, "organic", null, 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("certification"), eq("organic"));
    }

    @Test
    void findAll_withOnlyRoast_addsRoastJoin() {
        stubDbSpec();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();

        StepVerifier.create(adapter.findAll(null, null, null, null, null, null, "medium", 0, 10, null))
                .verifyComplete();

        verify(spec).bind(eq("roast"), eq("medium"));
    }

    @Test
    void countAll_withCertificationAndRoast_bindsFilters() {
        stubDbSpec();
        doReturn(Mono.just(2L)).when(rowsFetchSpec).one();

        StepVerifier.create(adapter.countAll(null, null, null, null, null, "organic", "medium"))
                .expectNext(2L)
                .verifyComplete();

        verify(spec).bind(eq("certification"), eq("organic"));
        verify(spec).bind(eq("roast"), eq("medium"));
    }

    @Test
    void save_withAllSubentities_returnsProduct() {
        CertificationData certData = CertificationData.builder().id(1).code("organic").build();

        when(template.insert(any(ProductData.class))).thenReturn(Mono.just(productData));
        when(template.insert(any(ProductImageData.class))).thenReturn(Mono.just(ProductImageData.builder().build()));
        when(template.insert(any(ProductPresentationData.class))).thenReturn(Mono.just(ProductPresentationData.builder().build()));
        when(template.insert(any(ProductFlavorNoteData.class))).thenReturn(Mono.just(ProductFlavorNoteData.builder().build()));
        when(template.insert(any(ProductCuppingData.class))).thenReturn(Mono.just(ProductCuppingData.builder().build()));
        when(certificationRepository.findByCode(anyString())).thenReturn(Mono.just(certData));
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

        ProductImage img = ProductImage.builder()
                .id(UUID.randomUUID()).productId(productId).imageUrl("http://img").displayOrder(1).build();
        ProductPresentation pres = ProductPresentation.builder()
                .id(UUID.randomUUID()).productId(productId).presentation("250g").extraPrice(BigDecimal.ZERO).build();
        ProductFlavorNote fn = ProductFlavorNote.builder()
                .id(UUID.randomUUID()).productId(productId).name("chocolate").icon("🍫").intensity((short) 3).build();
        ProductCupping cup = ProductCupping.builder()
                .productId(productId).score(new BigDecimal("85.0"))
                .aroma((short) 8).flavor((short) 8).body((short) 7).finish((short) 8).acidity((short) 7).build();

        Product product = Product.builder()
                .id(productId).producerId(producerId).name("Test Coffee")
                .price(new BigDecimal("15.00")).status(ProductStatus.active)
                .images(List.of(img)).presentations(List.of(pres))
                .flavorNotes(List.of(fn)).cupping(cup)
                .certificationCodes(List.of("organic"))
                .build();

        StepVerifier.create(adapter.save(product))
                .expectNextMatches(p -> productId.equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void findById_withCuppingPresent_mapsCupping() {
        ProductCuppingData cuppingData = ProductCuppingData.builder()
                .productId(productId).score(new BigDecimal("87.5"))
                .aroma((short) 8).flavor((short) 9).body((short) 7).finish((short) 8).acidity((short) 8).build();

        stubDbSpec();
        doReturn(Mono.just(productData)).when(rowsFetchSpec).one();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        when(imageRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(presentationRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(flavorNoteRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(cuppingRepository.findByProductId(productId)).thenReturn(Mono.just(cuppingData));

        StepVerifier.create(adapter.findById(productId))
                .expectNextMatches(p -> productId.equals(p.getId()) && p.getCupping() != null)
                .verifyComplete();
    }

    @Test
    void imageToData_mapsAllFields() {
        UUID imgId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        ProductImage img = ProductImage.builder()
                .id(imgId).productId(productId).imageUrl("http://img.jpg").displayOrder(2).uploadedAt(now).build();

        ProductImageData data = ProductRepositoryAdapter.imageToData(img, productId);

        assertThat(data.getId()).isEqualTo(imgId);
        assertThat(data.getProductId()).isEqualTo(productId);
        assertThat(data.getImageUrl()).isEqualTo("http://img.jpg");
        assertThat(data.getDisplayOrder()).isEqualTo(2);
        assertThat(data.getUploadedAt()).isEqualTo(now);
    }

    @Test
    void imageToDomain_mapsAllFields() {
        UUID imgId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        ProductImageData data = ProductImageData.builder()
                .id(imgId).productId(productId).imageUrl("http://img.jpg").displayOrder(3).uploadedAt(now).build();

        ProductImage img = ProductRepositoryAdapter.imageToDomain(data);

        assertThat(img.getId()).isEqualTo(imgId);
        assertThat(img.getProductId()).isEqualTo(productId);
        assertThat(img.getImageUrl()).isEqualTo("http://img.jpg");
        assertThat(img.getDisplayOrder()).isEqualTo(3);
        assertThat(img.getUploadedAt()).isEqualTo(now);
    }

    @Test
    void presentationToData_mapsAllFields() {
        UUID presId = UUID.randomUUID();
        ProductPresentation pres = ProductPresentation.builder()
                .id(presId).productId(productId).presentation("500g").extraPrice(new BigDecimal("2.50")).build();

        ProductPresentationData data = ProductRepositoryAdapter.presentationToData(pres, productId);

        assertThat(data.getId()).isEqualTo(presId);
        assertThat(data.getProductId()).isEqualTo(productId);
        assertThat(data.getPresentation()).isEqualTo("500g");
        assertThat(data.getExtraPrice()).isEqualByComparingTo(new BigDecimal("2.50"));
    }

    @Test
    void presentationToDomain_mapsAllFields() {
        UUID presId = UUID.randomUUID();
        ProductPresentationData data = ProductPresentationData.builder()
                .id(presId).productId(productId).presentation("250g").extraPrice(new BigDecimal("1.00")).build();

        ProductPresentation pres = ProductRepositoryAdapter.presentationToDomain(data);

        assertThat(pres.getId()).isEqualTo(presId);
        assertThat(pres.getProductId()).isEqualTo(productId);
        assertThat(pres.getPresentation()).isEqualTo("250g");
        assertThat(pres.getExtraPrice()).isEqualByComparingTo(new BigDecimal("1.00"));
    }

    @Test
    void flavorNoteToData_mapsAllFields() {
        UUID fnId = UUID.randomUUID();
        ProductFlavorNote fn = ProductFlavorNote.builder()
                .id(fnId).productId(productId).name("caramel").icon("🍮").intensity((short) 4).build();

        ProductFlavorNoteData data = ProductRepositoryAdapter.flavorNoteToData(fn, productId);

        assertThat(data.getId()).isEqualTo(fnId);
        assertThat(data.getProductId()).isEqualTo(productId);
        assertThat(data.getName()).isEqualTo("caramel");
        assertThat(data.getIcon()).isEqualTo("🍮");
        assertThat(data.getIntensity()).isEqualTo((short) 4);
    }

    @Test
    void flavorNoteToDomain_mapsAllFields() {
        UUID fnId = UUID.randomUUID();
        ProductFlavorNoteData data = ProductFlavorNoteData.builder()
                .id(fnId).productId(productId).name("citrus").icon("🍋").intensity((short) 5).build();

        ProductFlavorNote fn = ProductRepositoryAdapter.flavorNoteToDomain(data);

        assertThat(fn.getId()).isEqualTo(fnId);
        assertThat(fn.getProductId()).isEqualTo(productId);
        assertThat(fn.getName()).isEqualTo("citrus");
        assertThat(fn.getIcon()).isEqualTo("🍋");
        assertThat(fn.getIntensity()).isEqualTo((short) 5);
    }

    @Test
    void cuppingToData_mapsAllFields() {
        ProductCupping cupping = ProductCupping.builder()
                .productId(productId).score(new BigDecimal("88.5"))
                .aroma((short) 9).flavor((short) 8).body((short) 7).finish((short) 8).acidity((short) 9).build();

        ProductCuppingData data = ProductRepositoryAdapter.cuppingToData(cupping, productId);

        assertThat(data.getProductId()).isEqualTo(productId);
        assertThat(data.getScore()).isEqualByComparingTo(new BigDecimal("88.5"));
        assertThat(data.getAroma()).isEqualTo((short) 9);
        assertThat(data.getFlavor()).isEqualTo((short) 8);
        assertThat(data.getBody()).isEqualTo((short) 7);
        assertThat(data.getFinish()).isEqualTo((short) 8);
        assertThat(data.getAcidity()).isEqualTo((short) 9);
    }

    @Test
    void cuppingToDomain_mapsAllFields() {
        ProductCuppingData data = ProductCuppingData.builder()
                .productId(productId).score(new BigDecimal("90.0"))
                .aroma((short) 9).flavor((short) 9).body((short) 8).finish((short) 9).acidity((short) 8).build();

        ProductCupping cupping = ProductRepositoryAdapter.cuppingToDomain(data);

        assertThat(cupping.getProductId()).isEqualTo(productId);
        assertThat(cupping.getScore()).isEqualByComparingTo(new BigDecimal("90.0"));
        assertThat(cupping.getAroma()).isEqualTo((short) 9);
        assertThat(cupping.getFlavor()).isEqualTo((short) 9);
        assertThat(cupping.getBody()).isEqualTo((short) 8);
        assertThat(cupping.getFinish()).isEqualTo((short) 9);
        assertThat(cupping.getAcidity()).isEqualTo((short) 8);
    }

    @Test
    void update_withNonEmptyCertificationCodes_replacesAndReturns() {
        CertificationData certData = CertificationData.builder().id(1).code("organic").build();

        when(template.update(any(Query.class), any(Update.class), eq(ProductData.class))).thenReturn(Mono.just(1L));
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.fetch()).thenReturn(fSpec);
        when(fSpec.rowsUpdated()).thenReturn(Mono.just(1L));
        when(certificationRepository.findByCode(anyString())).thenReturn(Mono.just(certData));
        doReturn(rowsFetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(productData)).when(rowsFetchSpec).one();
        doReturn(Flux.empty()).when(rowsFetchSpec).all();
        when(imageRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(presentationRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(flavorNoteRepository.findByProductId(productId)).thenReturn(Flux.empty());
        when(cuppingRepository.findByProductId(productId)).thenReturn(Mono.empty());

        Product updateProduct = Product.builder()
                .id(productId).producerId(producerId).name("Updated Coffee")
                .price(new BigDecimal("20.00")).status(ProductStatus.active)
                .certificationCodes(List.of("organic"))
                .stock(10)
                .build();

        StepVerifier.create(adapter.update(updateProduct))
                .expectNextMatches(p -> productId.equals(p.getId()))
                .verifyComplete();
    }
}
