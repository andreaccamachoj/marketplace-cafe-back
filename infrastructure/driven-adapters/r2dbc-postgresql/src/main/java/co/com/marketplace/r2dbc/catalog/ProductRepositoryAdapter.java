package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductCupping;
import co.com.marketplace.model.catalog.ProductFlavorNote;
import co.com.marketplace.model.catalog.ProductImage;
import co.com.marketplace.model.catalog.ProductPresentation;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductGateway {

    private final ProductReactiveRepository repository;
    private final ProductImageReactiveRepository imageRepository;
    private final ProductPresentationReactiveRepository presentationRepository;
    private final ProductFlavorNoteReactiveRepository flavorNoteRepository;
    private final ProductCuppingReactiveRepository cuppingRepository;
    private final CertificationReactiveRepository certificationRepository;
    private final R2dbcEntityTemplate template;
    private final DatabaseClient databaseClient;

    private static final String ENRICHED_COLS =
            ", COALESCE((SELECT AVG(r.rating::double precision) FROM marketplace.reviews r" +
            "  WHERE r.product_id = p.id AND r.status = 'published'), 0.0) AS avg_rating" +
            ", COALESCE((SELECT COUNT(r.id) FROM marketplace.reviews r" +
            "  WHERE r.product_id = p.id AND r.status = 'published'), 0)::int AS review_count" +
            ", COALESCE((SELECT SUM(i.quantity) FROM marketplace.inventory i" +
            "  WHERE i.product_id = p.id), 0)::int AS stock_qty";

    @Override
    public Mono<Product> save(Product product) {
        ProductData data = toData(product);
        // Bug 1: force INSERT via template.insert() — avoids false UPDATE when UUID is pre-set
        return template.insert(data)
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#save] DB request: producerId={}", product.getProducerId()))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#save] DB response: id={}", r.getId()))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .flatMap(saved -> {
                    UUID pid = saved.getId();
                    List<Mono<?>> ops = new ArrayList<>();
                    if (product.getImages() != null) {
                        product.getImages().forEach(img ->
                                ops.add(template.insert(imageToData(img, pid))));
                    }
                    if (product.getPresentations() != null) {
                        product.getPresentations().forEach(p ->
                                ops.add(template.insert(presentationToData(p, pid))));
                    }
                    if (product.getFlavorNotes() != null) {
                        product.getFlavorNotes().forEach(fn ->
                                ops.add(template.insert(flavorNoteToData(fn, pid))));
                    }
                    if (product.getCupping() != null) {
                        ops.add(template.insert(cuppingToData(product.getCupping(), pid)));
                    }
                    if (product.getCertificationCodes() != null) {
                        product.getCertificationCodes().forEach(code ->
                                ops.add(certificationRepository.findByCode(code)
                                        .flatMap(cert -> databaseClient.sql(
                                                "INSERT INTO marketplace.product_certifications (product_id, certification_id) VALUES (:pid, :certId) ON CONFLICT DO NOTHING")
                                                .bind("pid", pid)
                                                .bind("certId", cert.getId())
                                                .fetch().rowsUpdated())));
                    }
                    if (ops.isEmpty()) return Mono.just(saved);
                    return Mono.when(ops).thenReturn(saved);
                })
                // Bug 3: return fully hydrated domain entity instead of shallow mapping
                .flatMap(saved -> findById(saved.getId()));
    }

    @Override
    public Mono<Product> findById(UUID id) {
        String sql = "SELECT p.*, u.full_name AS producer_name, cat.name AS category_name" + ENRICHED_COLS +
                " FROM marketplace.products p" +
                " LEFT JOIN marketplace.users u ON u.id = p.producer_id" +
                " LEFT JOIN marketplace.categories cat ON cat.id = p.category_id" +
                " WHERE p.id = :id";
        return databaseClient.sql(sql).bind("id", id)
                .map((row, meta) -> mapRowEnriched(row)).one()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .flatMap(data -> {
                    UUID pid = data.getId();
                    Mono<List<ProductImage>> images = imageRepository.findByProductId(pid)
                            .map(ProductRepositoryAdapter::imageToDomain).collectList();
                    Mono<List<ProductPresentation>> presentations = presentationRepository.findByProductId(pid)
                            .map(ProductRepositoryAdapter::presentationToDomain).collectList();
                    Mono<List<ProductFlavorNote>> flavorNotes = flavorNoteRepository.findByProductId(pid)
                            .map(ProductRepositoryAdapter::flavorNoteToDomain).collectList();
                    // Bug 2: wrap in Optional so Mono.zip never receives an empty Mono or null
                    Mono<Optional<ProductCupping>> cupping = cuppingRepository.findByProductId(pid)
                            .map(d -> Optional.of(cuppingToDomain(d)))
                            .defaultIfEmpty(Optional.empty());
                    Mono<List<Integer>> roastIds = databaseClient
                            .sql("SELECT roast_level_id FROM marketplace.product_roast_levels WHERE product_id = :pid")
                            .bind("pid", pid)
                            .map((row, meta) -> row.get("roast_level_id", Integer.class))
                            .all().collectList();
                    Mono<List<String>> certCodes = databaseClient
                            .sql("SELECT c.code FROM marketplace.product_certifications pc" +
                                 " JOIN marketplace.certifications c ON c.id = pc.certification_id" +
                                 " WHERE pc.product_id = :pid")
                            .bind("pid", pid)
                            .map((row, meta) -> row.get("code", String.class))
                            .all().collectList();
                    return Mono.zip(images, presentations, flavorNotes, cupping, roastIds, certCodes)
                            .map(t -> toDomain(data, t.getT1(), t.getT2(), t.getT3(),
                                    t.getT4().orElse(null), t.getT5(), t.getT6()));
                });
    }

    @Override
    public Mono<Product> update(Product product) {
        return template.update(
                Query.query(Criteria.where("id").is(product.getId())),
                Update.update("name", product.getName())
                        .set("description", product.getDescription())
                        .set("price", product.getPrice())
                        .set("original_price", product.getOriginalPrice())
                        .set("discount_percent", product.getDiscountPercent())
                        .set("unit", product.getUnit())
                        .set("region", product.getRegion())
                        .set("emoji", product.getEmoji())
                        .set("category_id", product.getCategoryId())
                        .set("status", product.getStatus() != null ? product.getStatus().name() : null)
                        .set("updated_at", OffsetDateTime.now()),
                ProductData.class
        ).doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#update] DB request: id={}", product.getId()))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#update] DB response: result={}", r))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .then(replaceCertifications(product.getId(), product.getCertificationCodes()))
                .then(upsertInventory(product.getId(), product.getStock()))
                .then(findById(product.getId()));
    }

    @Override
    public Mono<Void> updateStatus(UUID id, ProductStatus status) {
        return template.update(
                Query.query(Criteria.where("id").is(id)),
                Update.update("status", status.name()).set("updated_at", OffsetDateTime.now()),
                ProductData.class
        ).doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#updateStatus] DB request: id={}, status={}", id, status))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#updateStatus] DB response: result={}", r))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#updateStatus] DB error: {}", e.getMessage()))
                .then();
    }

    @Override
    public Flux<Product> findAll(String search, UUID categoryId, String region,
                                 BigDecimal minPrice, BigDecimal maxPrice,
                                 String certification, String roast,
                                 int page, int size, String sort) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT p.*, u.full_name AS producer_name, cat.name AS category_name" + ENRICHED_COLS +
                " FROM marketplace.products p" +
                " LEFT JOIN marketplace.users u ON u.id = p.producer_id" +
                " LEFT JOIN marketplace.categories cat ON cat.id = p.category_id");
        if (certification != null) {
            sql.append(" JOIN marketplace.product_certifications pc ON pc.product_id = p.id");
            sql.append(" JOIN marketplace.certifications c ON c.id = pc.certification_id AND c.code = :certification");
        }
        if (roast != null) {
            sql.append(" JOIN marketplace.product_roast_levels prl ON prl.product_id = p.id");
            sql.append(" JOIN marketplace.roast_levels rl ON rl.id = prl.roast_level_id AND rl.code = :roast");
        }
        sql.append(" WHERE 1=1");
        if (search != null) sql.append(" AND (p.name ILIKE :search OR p.description ILIKE :search)");
        if (categoryId != null) sql.append(" AND p.category_id = :categoryId");
        if (region != null) sql.append(" AND p.region = :region");
        if (minPrice != null) sql.append(" AND p.price >= :minPrice");
        if (maxPrice != null) sql.append(" AND p.price <= :maxPrice");
        String orderBy = "sold_count".equals(sort) ? "p.sold_count DESC"
                : "price_asc".equals(sort) ? "p.price ASC"
                : "price_desc".equals(sort) ? "p.price DESC"
                : "p.created_at DESC";
        sql.append(" ORDER BY ").append(orderBy);
        sql.append(" LIMIT :size OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (search != null) spec = spec.bind("search", "%" + search + "%");
        if (categoryId != null) spec = spec.bind("categoryId", categoryId);
        if (region != null) spec = spec.bind("region", region);
        if (minPrice != null) spec = spec.bind("minPrice", minPrice);
        if (maxPrice != null) spec = spec.bind("maxPrice", maxPrice);
        if (certification != null) spec = spec.bind("certification", certification);
        if (roast != null) spec = spec.bind("roast", roast);
        spec = spec.bind("size", size).bind("offset", (long) page * size);

        return spec.map((row, meta) -> mapRowEnriched(row)).all()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#findAll] DB request: page={}, size={}, search={}", page, size, search))
                .doOnComplete(() -> log.debug("[ProductRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(ProductRepositoryAdapter::toDomainShallow);
    }

    @Override
    public Mono<Long> countAll(String search, UUID categoryId, String region,
                               BigDecimal minPrice, BigDecimal maxPrice,
                               String certification, String roast) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT p.id) FROM marketplace.products p");
        if (certification != null) {
            sql.append(" JOIN marketplace.product_certifications pc ON pc.product_id = p.id");
            sql.append(" JOIN marketplace.certifications c ON c.id = pc.certification_id AND c.code = :certification");
        }
        if (roast != null) {
            sql.append(" JOIN marketplace.product_roast_levels prl ON prl.product_id = p.id");
            sql.append(" JOIN marketplace.roast_levels rl ON rl.id = prl.roast_level_id AND rl.code = :roast");
        }
        sql.append(" WHERE 1=1");
        if (search != null) sql.append(" AND (p.name ILIKE :search OR p.description ILIKE :search)");
        if (categoryId != null) sql.append(" AND p.category_id = :categoryId");
        if (region != null) sql.append(" AND p.region = :region");
        if (minPrice != null) sql.append(" AND p.price >= :minPrice");
        if (maxPrice != null) sql.append(" AND p.price <= :maxPrice");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (search != null) spec = spec.bind("search", "%" + search + "%");
        if (categoryId != null) spec = spec.bind("categoryId", categoryId);
        if (region != null) spec = spec.bind("region", region);
        if (minPrice != null) spec = spec.bind("minPrice", minPrice);
        if (maxPrice != null) spec = spec.bind("maxPrice", maxPrice);
        if (certification != null) spec = spec.bind("certification", certification);
        if (roast != null) spec = spec.bind("roast", roast);

        return spec.map((row, meta) -> row.get(0, Long.class)).one()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#countAll] DB request: search={}, categoryId={}", search, categoryId))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#countAll] DB response: result={}", r))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#countAll] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<Product> findFeatured(int limit) {
        String sql = "SELECT p.*, u.full_name AS producer_name, cat.name AS category_name" + ENRICHED_COLS +
                " FROM marketplace.products p" +
                " LEFT JOIN marketplace.users u ON u.id = p.producer_id" +
                " LEFT JOIN marketplace.categories cat ON cat.id = p.category_id" +
                " WHERE p.status = 'active' ORDER BY p.sold_count DESC LIMIT :limit";
        return databaseClient.sql(sql).bind("limit", limit)
                .map((row, meta) -> mapRowEnriched(row)).all()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#findFeatured] DB request: limit={}", limit))
                .doOnComplete(() -> log.debug("[ProductRepositoryAdapter#findFeatured] DB response: complete"))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#findFeatured] DB error: {}", e.getMessage()))
                .map(ProductRepositoryAdapter::toDomainShallow);
    }

    @Override
    public Flux<Product> findByProducerId(UUID producerId, ProductStatus status, int page, int size) {
        String statusCondition = status != null ? " AND p.status = :status" : "";
        String sql = "SELECT p.*, u.full_name AS producer_name, cat.name AS category_name" + ENRICHED_COLS +
                " FROM marketplace.products p" +
                " LEFT JOIN marketplace.producer_profiles pp ON pp.id = p.producer_id" +
                " LEFT JOIN marketplace.users u ON u.id = pp.user_id" +
                " LEFT JOIN marketplace.categories cat ON cat.id = p.category_id" +
                " WHERE p.producer_id = :producerId" + statusCondition +
                " ORDER BY p.created_at DESC LIMIT :size OFFSET :offset";
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("producerId", producerId)
                .bind("size", size)
                .bind("offset", (long) page * size);
        if (status != null) spec = spec.bind("status", status.name());
        return spec.map((row, meta) -> mapRowEnriched(row)).all()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#findByProducerId] DB request: producerId={}, status={}", producerId, status))
                .doOnComplete(() -> log.debug("[ProductRepositoryAdapter#findByProducerId] DB response: complete"))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(ProductRepositoryAdapter::toDomainShallow);
    }

    @Override
    public Flux<Product> findAllForAdmin(int page, int size) {
        String sql = "SELECT p.*, u.full_name AS producer_name, cat.name AS category_name" + ENRICHED_COLS +
                " FROM marketplace.products p" +
                " LEFT JOIN marketplace.users u ON u.id = p.producer_id" +
                " LEFT JOIN marketplace.categories cat ON cat.id = p.category_id" +
                " ORDER BY p.created_at DESC LIMIT :size OFFSET :offset";
        return databaseClient.sql(sql)
                .bind("size", size)
                .bind("offset", (long) page * size)
                .map((row, meta) -> mapRowEnriched(row)).all()
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#findAllForAdmin] DB request"))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#findAllForAdmin] DB error: {}", e.getMessage()))
                .map(ProductRepositoryAdapter::toDomainShallow);
    }

    @Override
    public Mono<Long> countByProducerId(UUID producerId, ProductStatus status) {
        String statusStr = status != null ? status.name() : null;
        return repository.countByProducerIdAndStatus(producerId, statusStr)
                .doOnSubscribe(s -> log.debug("[ProductRepositoryAdapter#countByProducerId] DB request: producerId={}, status={}", producerId, status))
                .doOnSuccess(r -> log.debug("[ProductRepositoryAdapter#countByProducerId] DB response: result={}", r))
                .doOnError(e -> log.error("[ProductRepositoryAdapter#countByProducerId] DB error: {}", e.getMessage()));
    }

    private Mono<Void> replaceCertifications(UUID productId, List<String> codes) {
        Mono<Long> delete = databaseClient.sql(
                "DELETE FROM marketplace.product_certifications WHERE product_id = :pid")
                .bind("pid", productId)
                .fetch().rowsUpdated();
        if (codes == null || codes.isEmpty()) return delete.then();
        return delete.thenMany(
                Flux.fromIterable(codes).flatMap(code ->
                        certificationRepository.findByCode(code)
                                .flatMap(cert -> databaseClient.sql(
                                        "INSERT INTO marketplace.product_certifications (product_id, certification_id) VALUES (:pid, :certId)")
                                        .bind("pid", productId)
                                        .bind("certId", cert.getId())
                                        .fetch().rowsUpdated())))
                .then();
    }

    private Mono<Void> upsertInventory(UUID productId, int quantity) {
        return databaseClient.sql(
                "INSERT INTO marketplace.inventory (product_id, quantity, updated_at) " +
                "VALUES (:productId, :quantity, NOW()) " +
                "ON CONFLICT (product_id) DO UPDATE SET quantity = EXCLUDED.quantity, updated_at = NOW()")
                .bind("productId", productId)
                .bind("quantity", quantity)
                .fetch().rowsUpdated().then();
    }

    private static ProductData mapRow(io.r2dbc.spi.Row row) {
        return ProductData.builder()
                .id(row.get("id", UUID.class))
                .producerId(row.get("producer_id", UUID.class))
                .categoryId(row.get("category_id", UUID.class))
                .name(row.get("name", String.class))
                .description(row.get("description", String.class))
                .price(row.get("price", BigDecimal.class))
                .originalPrice(row.get("original_price", BigDecimal.class))
                .discountPercent(row.get("discount_percent", BigDecimal.class))
                .unit(row.get("unit", String.class))
                .region(row.get("region", String.class))
                .emoji(row.get("emoji", String.class))
                .soldCount(row.get("sold_count", Integer.class))
                .status(row.get("status", String.class))
                .createdAt(row.get("created_at", OffsetDateTime.class))
                .updatedAt(row.get("updated_at", OffsetDateTime.class))
                .build();
    }

    private static ProductData mapRowEnriched(io.r2dbc.spi.Row row) {
        ProductData d = mapRow(row);
        d.setProducerName(row.get("producer_name", String.class));
        d.setCategoryName(row.get("category_name", String.class));
        Double avgRating = row.get("avg_rating", Double.class);
        Integer reviewCount = row.get("review_count", Integer.class);
        Integer stockQty = row.get("stock_qty", Integer.class);
        d.setRating(avgRating != null ? avgRating : 0.0);
        d.setReviewCount(reviewCount != null ? reviewCount : 0);
        d.setStock(stockQty != null ? stockQty : 0);
        return d;
    }

    static Product toDomainShallow(ProductData d) {
        return toDomain(d, null, null, null, null, null, null);
    }

    static Product toDomain(ProductData d,
                             List<ProductImage> images,
                             List<ProductPresentation> presentations,
                             List<ProductFlavorNote> flavorNotes,
                             ProductCupping cupping,
                             List<Integer> roastLevelIds,
                             List<String> certificationCodes) {
        return Product.builder()
                .id(d.getId())
                .producerId(d.getProducerId())
                .producerName(d.getProducerName())
                .categoryId(d.getCategoryId())
                .categoryName(d.getCategoryName())
                .name(d.getName())
                .description(d.getDescription())
                .price(d.getPrice())
                .originalPrice(d.getOriginalPrice())
                .discountPercent(d.getDiscountPercent())
                .unit(d.getUnit())
                .region(d.getRegion())
                .emoji(d.getEmoji())
                .soldCount(d.getSoldCount())
                .status(d.getStatus() != null ? ProductStatus.valueOf(d.getStatus()) : null)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .rating(d.getRating())
                .reviewCount(d.getReviewCount())
                .stock(d.getStock())
                .images(images)
                .presentations(presentations)
                .flavorNotes(flavorNotes)
                .cupping(cupping)
                .roastLevelIds(roastLevelIds)
                .certificationCodes(certificationCodes)
                .build();
    }

    static ProductData toData(Product p) {
        return ProductData.builder()
                .id(p.getId())
                .producerId(p.getProducerId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .originalPrice(p.getOriginalPrice())
                .discountPercent(p.getDiscountPercent())
                .unit(p.getUnit())
                .region(p.getRegion())
                .emoji(p.getEmoji())
                .soldCount(p.getSoldCount())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    static ProductImage imageToDomain(ProductImageData d) {
        return ProductImage.builder()
                .id(d.getId())
                .productId(d.getProductId())
                .imageUrl(d.getImageUrl())
                .displayOrder(d.getDisplayOrder())
                .uploadedAt(d.getUploadedAt())
                .build();
    }

    static ProductImageData imageToData(ProductImage img, UUID productId) {
        return ProductImageData.builder()
                .id(img.getId())
                .productId(productId)
                .imageUrl(img.getImageUrl())
                .displayOrder(img.getDisplayOrder())
                .uploadedAt(img.getUploadedAt())
                .build();
    }

    static ProductPresentation presentationToDomain(ProductPresentationData d) {
        return ProductPresentation.builder()
                .id(d.getId())
                .productId(d.getProductId())
                .presentation(d.getPresentation())
                .extraPrice(d.getExtraPrice())
                .build();
    }

    static ProductPresentationData presentationToData(ProductPresentation p, UUID productId) {
        return ProductPresentationData.builder()
                .id(p.getId())
                .productId(productId)
                .presentation(p.getPresentation())
                .extraPrice(p.getExtraPrice())
                .build();
    }

    static ProductFlavorNote flavorNoteToDomain(ProductFlavorNoteData d) {
        return ProductFlavorNote.builder()
                .id(d.getId())
                .productId(d.getProductId())
                .name(d.getName())
                .icon(d.getIcon())
                .intensity(d.getIntensity())
                .build();
    }

    static ProductFlavorNoteData flavorNoteToData(ProductFlavorNote fn, UUID productId) {
        return ProductFlavorNoteData.builder()
                .id(fn.getId())
                .productId(productId)
                .name(fn.getName())
                .icon(fn.getIcon())
                .intensity(fn.getIntensity())
                .build();
    }

    static ProductCupping cuppingToDomain(ProductCuppingData d) {
        return ProductCupping.builder()
                .productId(d.getProductId())
                .score(d.getScore())
                .aroma(d.getAroma())
                .flavor(d.getFlavor())
                .body(d.getBody())
                .finish(d.getFinish())
                .acidity(d.getAcidity())
                .build();
    }

    static ProductCuppingData cuppingToData(ProductCupping c, UUID productId) {
        return ProductCuppingData.builder()
                .productId(productId)
                .score(c.getScore())
                .aroma(c.getAroma())
                .flavor(c.getFlavor())
                .body(c.getBody())
                .finish(c.getFinish())
                .acidity(c.getAcidity())
                .build();
    }
}
