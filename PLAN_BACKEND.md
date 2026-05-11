# PLAN DE TRABAJO — BACKEND `marketplace-cafe-back`

> **Documento de referencia técnica** — Proyecto de grado UNAB
> Versión 1.0 · 2026-05-01
> Stack: Spring Boot 4.0.5 · WebFlux · R2DBC PostgreSQL · Java 21 · Gradle multi-módulo · Clean Architecture (scaffold Bancolombia)

---

## ÍNDICE

- [FASE 0 — Comprensión del contexto](#fase-0--comprensión-del-contexto)
- [FASE 1 — Configuración del entorno backend](#fase-1--configuración-del-entorno-backend)
- [FASE 2 — Definición del Dominio](#fase-2--definición-del-dominio-clean-architecture)
- [FASE 3 — Casos de Uso](#fase-3--casos-de-uso-application-layer)
- [FASE 4 — Adaptadores e Infraestructura](#fase-4--adaptadores-e-infraestructura)
- [FASE 5 — Endpoints requeridos por el Frontend](#fase-5--endpoints-requeridos-por-el-frontend)
- [FASE 6 — Integración Front ↔ Back](#fase-6--integración-front--back)
- [FASE 7 — Pruebas y Validación](#fase-7--pruebas-y-validación)
- [Convenciones globales](#convenciones-globales)

---

## FASE 0 — Comprensión del contexto

### 0.1 Propósito del proyecto

**World Coffee Marketplace (WCM)** es un marketplace B2C/B2B de café especial colombiano que conecta:
- **Compradores (`buyer`)**: consumidores finales que compran café por kilogramos.
- **Productores (`producer`)**: caficultores que registran fincas, productos y procesan pedidos.
- **Administradores (`admin`)**: aprueban productores, gestionan categorías, supervisan actividad.

El backend es un **microservicio reactivo** (WebFlux) que expone una API REST consumida por el frontend Angular 19 (`marketplace-cafe-front`). El microservicio es **stateless** (autenticación JWT) y persiste en PostgreSQL vía R2DBC.

### 0.2 Mapa de funcionalidades del frontend que requieren integración

| Módulo Front | Servicio Angular | Bounded Context Backend |
|--------------|-----------------|--------------------------|
| Auth (login, registro, recuperación) | `AuthService`, `TokenStorageService` | Identity |
| Direcciones del comprador | `AddressService` | Identity |
| Perfil comprador | `BuyerProfileService` | Identity |
| Perfil productor + finca | `ProducerProfileService`, `FarmService` | Producer / Farm |
| Catálogo público | `ProductService`, `CategoryService` | Catalog |
| Detalle producto + reseñas | `ProductService`, `ReviewService` | Catalog / Reviews |
| Carrito | `CartService` | Cart |
| Favoritos | `FavoritesService` | Favorites |
| Pedidos comprador | `OrderService` | Orders |
| Productos del productor (CRUD) | `ProducerProductService` | Catalog |
| Pedidos del productor | `ProducerOrderService` | Orders |
| Reseñas del productor | `ProducerReviewService` | Reviews |
| Admin: usuarios | `AdminUserService` | Admin |
| Admin: categorías | `AdminCategoryService` | Catalog |
| Admin: aprobaciones de productores | `ProducerApprovalService` | Admin |
| Admin: log de actividad | `AdminActivityService` | Admin |
| Notificaciones | `NotificationService` | Notifications |

### 0.3 Modelo de dominio (`db_schema_v3.sql`)

Esquema PostgreSQL **`marketplace`** con **39 tablas** agrupadas en 11 contextos:

```
┌─ Identity & Access ────────────────────────────────────────┐
│ roles · users · user_roles · buyer_profiles                │
│ producer_profiles · producer_documents                     │
│ password_reset_tokens · privacy_consents · addresses       │
└────────────────────────────────────────────────────────────┘
┌─ Catalog ──────────────────────────────────────────────────┐
│ categories · products · product_images                     │
│ product_certifications · product_presentations             │
│ product_roast_levels · product_flavor_notes                │
│ product_cupping · certifications · roast_levels            │
└────────────────────────────────────────────────────────────┘
┌─ Farm ───────┐ ┌─ Inventory ─┐ ┌─ Cart ────────────────────┐
│ farms        │ │ inventory   │ │ carts · cart_items        │
│ farm_certifs │ │             │ │ coupons · shipping_options│
└──────────────┘ └─────────────┘ └───────────────────────────┘
┌─ Favorites ──┐ ┌─ Orders ─────────────────────────────────┐
│ favorites    │ │ orders · order_items · order_status_hist │
│              │ │ payment_methods · order_payments         │
└──────────────┘ └──────────────────────────────────────────┘
┌─ Reviews ──────────────┐ ┌─ Admin ─────────────────────────┐
│ reviews · review_replies│ │ producer_approvals              │
│                        │ │ approval_documents              │
│                        │ │ admin_activity_log              │
└────────────────────────┘ └─────────────────────────────────┘
┌─ Notifications ─┐ ┌─ Audit ───────┐
│ notifications   │ │ audit_logs    │
└─────────────────┘ └───────────────┘
```

**Enums PostgreSQL nativos** (`marketplace.*`): `user_status`, `producer_status`, `order_status`, `payment_status`, `review_status`, `coupon_discount_type`, `doc_status`.
> Las demás columnas de "estado" (`products.status`, `admin_activity_log.type`, etc.) son `VARCHAR` con CHECK constraint, no ENUMs nativos — se mapean como `String` sin problema.

**Vistas materializables** disponibles: `v_products_available`, `v_order_summary`.

### ✅ Checklist FASE 0
- [ ] Equipo entiende el propósito del marketplace y los 3 roles
- [ ] Frontend explorado: rutas, servicios mock identificados
- [ ] `db_schema_v3.sql` ejecutado en PostgreSQL local
- [ ] ERD (`erd_marketplace_v2.mmd`) revisado
- [ ] PLAN_BACKEND.md revisado por el equipo
- [ ] **Commit**: `git commit -m "docs: add backend work plan"`

---

## FASE 1 — Configuración del entorno backend

### 1.1 Verificación de la estructura del scaffold

El scaffold de Bancolombia ya generó:

```
marketplace-cafe-back/
├── applications/app-service/         ← bootstrap, MainApplication, UseCasesConfig
├── domain/
│   ├── model/                        ← entidades + Gateway interfaces
│   └── usecase/                      ← casos de uso (POJO, sin Spring)
├── infrastructure/
│   ├── driven-adapters/              ← adapters salientes (R2DBC, JWT, Email...)
│   ├── entry-points/reactive-web/    ← RouterRest + Handler + CorsConfig + SecurityHeadersConfig
│   └── helpers/
├── deployment/                        ← Dockerfile, k8s manifests
├── settings.gradle (incluye :app-service, :model, :usecase, :reactive-web)
├── main.gradle (Java 21, Lombok, Reactor, Pitest, Jacoco)
└── build.gradle (módulo raíz)
```

**Acción**: ejecutar `./gradlew build` para validar que el scaffold compila antes de añadir código.

### 1.2 Dependencias a agregar

Editar **`applications/app-service/build.gradle`**:

```groovy
dependencies {
    implementation project(':model')
    implementation project(':usecase')
    implementation project(':reactive-web')
    implementation project(':r2dbc-postgresql')
    implementation project(':security-jwt')

    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'

    runtimeOnly 'org.postgresql:r2dbc-postgresql'
    runtimeOnly 'org.postgresql:postgresql'           // Flyway necesita JDBC

    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-database-postgresql'

    implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
    runtimeOnly  'io.jsonwebtoken:jjwt-impl:0.12.6'
    runtimeOnly  'io.jsonwebtoken:jjwt-jackson:0.12.6'

    implementation 'org.mapstruct:mapstruct:1.6.3'
    annotationProcessor 'org.mapstruct:mapstruct-processor:1.6.3'

    implementation 'org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0'
}
```

### 1.3 Crear módulos faltantes

```bash
# desde la raíz
./gradlew tasks                                                  # ver scaffolding tasks
./gradlew generateDrivenAdapter --type r2dbc                     # genera infrastructure/driven-adapters/r2dbc-postgresql
./gradlew generateDrivenAdapter --type secrets-manager-aws       # opcional
./gradlew generateDrivenAdapter --type generic --name security-jwt
```

Registrar los nuevos módulos en `settings.gradle`:

```groovy
include ':r2dbc-postgresql'
project(':r2dbc-postgresql').projectDir = file('./infrastructure/driven-adapters/r2dbc-postgresql')

include ':security-jwt'
project(':security-jwt').projectDir = file('./infrastructure/driven-adapters/security-jwt')
```

### 1.4 Conexión reactiva a PostgreSQL (R2DBC + Flyway)

**`applications/app-service/src/main/resources/application.yaml`**:

```yaml
spring:
  application:
    name: marketplacecafe
  r2dbc:
    url: r2dbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:wcm_db}
    username: ${DB_USER:wcm}
    password: ${DB_PASS:wcm_pass}
    properties:
      schema: marketplace
  flyway:
    enabled: true
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:wcm_db}
    user: ${DB_USER:wcm}
    password: ${DB_PASS:wcm_pass}
    schemas: marketplace
    locations: classpath:db/migration
    baseline-on-migrate: true

security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-ttl: 3600        # 1h
    refresh-token-ttl: 604800     # 7d

cors:
  allowed-origins: ${CORS_ORIGINS:http://localhost:4200}

logging:
  level:
    co.com.marketplace: DEBUG
    org.springframework.r2dbc: INFO
```

Copiar `db_schema_v3.sql` como **`V1__initial_schema.sql`** en `applications/app-service/src/main/resources/db/migration/`.

### 1.5 Configuración de R2DBC (UUID + enums + auditoría)

**⚠️ Implementación definitiva (2026-05-03)** — El driver `org.postgresql:r2dbc-postgresql` (v1.1.1) requiere que el `ConnectionFactory` se defina programáticamente para registrar `EnumCodec`. Spring Boot no puede inyectar codecs personalizados vía URL.

**`infrastructure/driven-adapters/r2dbc-postgresql/build.gradle`**:
```groovy
// DEBE ser implementation (no runtimeOnly) para compilar contra las clases del driver
implementation 'org.postgresql:r2dbc-postgresql'
```

**`r2dbc-postgresql/.../config/R2dbcConfig.java`**:

```java
@Configuration
@EnableR2dbcRepositories(basePackages = "co.com.marketplace.r2dbc")
@EnableR2dbcAuditing
public class R2dbcConfig {

    @Bean
    public ConnectionFactory connectionFactory(
            @Value("${DB_HOST:localhost}") String host,
            @Value("${DB_PORT:5432}") int port,
            @Value("${DB_NAME:wcm_db}") String database,
            @Value("${DB_USER:wcm}") String username,
            @Value("${DB_PASS:wcm_pass}") String password) {

        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host).port(port).database(database)
                        .username(username).password(password)
                        .schema("marketplace")
                        .codecRegistrar(EnumCodec.builder()
                                .withEnum("user_status",          UserStatusType.class)
                                .withEnum("producer_status",      ProducerStatusType.class)
                                .withEnum("order_status",         OrderStatusType.class)
                                .withEnum("payment_status",       PaymentStatusType.class)
                                .withEnum("review_status",        ReviewStatusType.class)
                                .withEnum("coupon_discount_type", CouponDiscountType.class)
                                .withEnum("doc_status",           DocStatusType.class)
                                .build())
                        .build());
    }
}
```

- **No** usar `UuidConverter`: el driver mapea `java.util.UUID ↔ uuid` nativo.
- **No** usar `R2dbcCustomConversions` para enums: `EnumCodec` opera al nivel del protocolo wire.
- Los 7 Java enums viven en `co.com.marketplace.r2dbc.type` con constantes en **minúscula** (ej: `active`, `pending_verification`) para que `name()` coincida con el valor del ENUM en PostgreSQL.

### 1.6 CORS

**`infrastructure/entry-points/reactive-web/.../config/CorsConfig.java`** (ya existe — actualizar):

```java
@Configuration
public class CorsConfig {
    @Value("${cors.allowed-origins}") private String allowedOrigins;

    @Bean
    public CorsWebFilter corsWebFilter() {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        cors.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setExposedHeaders(List.of("Authorization"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsWebFilter(source);
    }
}
```

### 1.7 Variables de entorno

Validar que `marketplace-cafe-back/` lea correctamente del `.env` raíz vía `docker-compose.yml`:

```yaml
backend:
  environment:
    DB_HOST: postgres
    DB_NAME: ${DB_NAME}
    DB_USER: ${DB_USER}
    DB_PASS: ${DB_PASS}
    JWT_SECRET: ${JWT_SECRET}
    CORS_ORIGINS: ${CORS_ORIGINS}
```

### 1.8 GlobalErrorHandler + SecurityConfig + JWT

Crear:
- `infrastructure/entry-points/reactive-web/.../config/GlobalErrorWebExceptionHandler.java` (mapea excepciones de dominio a HTTP).
- `infrastructure/entry-points/reactive-web/.../config/SecurityConfig.java` (`SecurityWebFilterChain`, `BCryptPasswordEncoder`, `ServerAuthenticationConverter` JWT).
- `infrastructure/driven-adapters/security-jwt/.../JwtTokenProvider.java` (generación + validación).

### ✅ Checklist FASE 1
- [ ] `./gradlew build` pasa sin errores
- [ ] Módulos `r2dbc-postgresql` y `security-jwt` creados y registrados
- [ ] `application.yaml` configurado, lee `.env`
- [ ] Flyway ejecuta `V1__initial_schema.sql` y crea schema `marketplace`
- [ ] `./gradlew :app-service:bootRun` arranca sin errores
- [ ] `GET /actuator/health` responde 200
- [ ] CORS permite peticiones desde `http://localhost:4200`
- [ ] JWT genera y valida tokens (test unitario)
- [ ] **Commit**: `git commit -m "chore: configure WebFlux, R2DBC, Flyway, JWT, CORS"`

---

## FASE 2 — Definición del Dominio (Clean Architecture)

### 2.1 Reglas obligatorias

- Modelos en `domain/model/{contexto}/` son **POJO** sin Spring/JPA/R2DBC.
- Usar **records** Java 21 para Value Objects y DTOs internos.
- Usar **sealed interfaces** para resultados polimórficos (ej: `LoginResult.Success | Failure`).
- Gateways (puertos) son **interfaces** en `domain/model/{contexto}/gateways/`.
- Métodos retornan `Mono<T>` o `Flux<T>` (nunca bloqueante).
- Excepciones de dominio extienden `DomainException` (clase abstracta sealed) en `domain/model/exceptions/`.

### 2.2 Bounded contexts y entidades por contexto

| Contexto | Entidades de dominio | Value Objects | Gateways |
|----------|---------------------|---------------|----------|
| **identity** | `User`, `Role`, `BuyerProfile`, `ProducerProfile`, `ProducerDocument`, `PasswordResetToken`, `PrivacyConsent`, `Address` | `Email`, `HashedPassword`, `PhoneNumber`, `UserStatus` (enum) | `UserGateway`, `RoleGateway`, `BuyerProfileGateway`, `ProducerProfileGateway`, `ProducerDocumentGateway`, `PasswordResetTokenGateway`, `AddressGateway` |
| **catalog** | `Product`, `Category`, `ProductImage`, `ProductPresentation`, `ProductCupping`, `Certification`, `RoastLevel` | `Money`, `ProductStatus` (enum), `Slug`, `FlavorNote` | `ProductGateway`, `CategoryGateway`, `CertificationGateway`, `RoastLevelGateway` |
| **farm** | `Farm`, `FarmCertification` | `Coordinates`, `Hectares` | `FarmGateway` |
| **inventory** | `InventoryItem` | `InventoryMovementType` (enum) | `InventoryGateway` |
| **cart** | `Cart`, `CartItem`, `Coupon`, `ShippingOption` | `CouponCode` | `CartGateway`, `CouponGateway`, `ShippingOptionGateway` |
| **favorites** | `Favorite` | — | `FavoriteGateway` |
| **orders** | `Order`, `OrderItem`, `OrderStatusHistory`, `OrderPayment` | `OrderStatus` (enum), `PaymentStatus` (enum), `OrderNumber` | `OrderGateway`, `OrderPaymentGateway`, `OrderStatusHistoryGateway` |
| **payments** | `PaymentMethod` | — | `PaymentMethodGateway` |
| **reviews** | `Review`, `ReviewReply` | `Rating` (1–5) | `ReviewGateway`, `ReviewReplyGateway` |
| **admin** | `ProducerApproval`, `ApprovalDocument`, `AdminActivityLog` | `ApprovalStatus` (enum), `ActivityAction` (enum) | `ProducerApprovalGateway`, `ActivityLogGateway` |
| **notifications** | `Notification` | `NotificationType` (enum) | `NotificationGateway` |
| **audit** | `AuditLog` | — | `AuditLogGateway` |

### 2.3 Ejemplo de entidad y gateway

**`domain/model/identity/User.java`**:

```java
package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class User {
    UUID id;
    String email;
    String hashedPassword;
    String firstName;
    String lastName;
    String phone;
    UserStatus status;
    OffsetDateTime emailVerifiedAt;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
```

**`domain/model/identity/gateways/UserGateway.java`**:

```java
package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.User;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UserGateway {
    Mono<User> save(User user);
    Mono<User> findById(UUID id);
    Mono<User> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<User> update(User user);
    Mono<Void> deleteById(UUID id);
}
```

### 2.4 Excepciones de dominio (sealed)

```java
public abstract sealed class DomainException extends RuntimeException
    permits NotFoundException, ConflictException, ValidationException, UnauthorizedException, ForbiddenException {

    private final String code;
    protected DomainException(String code, String message) { super(message); this.code = code; }
    public String getCode() { return code; }
}
```

Subclases concretas:
- `NotFoundException` → HTTP 404
- `ConflictException` → HTTP 409
- `ValidationException` → HTTP 400
- `UnauthorizedException` → HTTP 401
- `ForbiddenException` → HTTP 403

### ✅ Checklist FASE 2
- [ ] 12 contextos creados en `domain/model/`
- [ ] ~50 entidades de dominio definidas como `@Value`/records
- [ ] ~25 Gateway interfaces definidas, todas reactivas
- [ ] Jerarquía de `DomainException` con sealed classes
- [ ] `./gradlew :model:test` pasa
- [ ] **Commit**: `git commit -m "feat(domain): define entities, value objects and gateways"`

---

## FASE 3 — Casos de Uso (Application Layer)

### 3.1 Reglas

- Una clase **final** por caso de uso, sin anotaciones Spring (cero acoplamiento al framework).
- **Constructor injection** explícito (no `@Autowired`).
- Un único método público (`execute(...)` o nombre semántico). SRP estricto.
- Retorna `Mono<T>` o `Flux<T>`.
- Las dependencias son **gateways** (interfaces), nunca implementaciones concretas (DIP).
- Validaciones de negocio dentro del use case; validaciones de formato en handler.

### 3.2 Inventario de casos de uso (≈ 90 casos)

#### Identity (12)
`RegisterBuyerUseCase`, `RegisterProducerUseCase`, `LoginUseCase`, `LogoutUseCase`, `RefreshTokenUseCase`, `RequestPasswordResetUseCase`, `ConfirmPasswordResetUseCase`, `GetCurrentUserUseCase`, `UpdateBuyerProfileUseCase`, `UpdateProducerProfileUseCase`, `ChangePasswordUseCase`, `RecordPrivacyConsentUseCase`.

#### Addresses (5)
`ListUserAddressesUseCase`, `CreateAddressUseCase`, `UpdateAddressUseCase`, `DeleteAddressUseCase`, `SetDefaultAddressUseCase`.

#### Catalog (10)
`ListProductsUseCase` (con filtros + paginación), `GetProductByIdUseCase`, `GetFeaturedProductsUseCase`, `ListCategoriesUseCase`, `GetCategoryBySlugUseCase`, `ListCertificationsUseCase`, `ListRoastLevelsUseCase`, `SearchProductsUseCase`, `GetProductsByProducerUseCase`, `GetProductBySlugUseCase`.

#### Producer Catalog (5)
`CreateProductUseCase`, `UpdateProductUseCase`, `ArchiveProductUseCase`, `ListMyProductsUseCase`, `UploadProductImageUseCase`.

#### Farm (3)
`GetFarmProfileUseCase`, `UpdateFarmProfileUseCase`, `LinkFarmCertificationUseCase`.

#### Inventory (3)
`AdjustInventoryUseCase`, `GetInventoryByProductUseCase`, `LogInventoryMovementUseCase`.

#### Cart (8)
`GetCartUseCase`, `AddCartItemUseCase`, `UpdateCartItemQuantityUseCase`, `RemoveCartItemUseCase`, `ClearCartUseCase`, `ApplyCouponUseCase`, `RemoveCouponUseCase`, `SelectShippingOptionUseCase`.

#### Favorites (3)
`ListFavoritesUseCase`, `AddFavoriteUseCase`, `RemoveFavoriteUseCase`.

#### Orders (10)
`PlaceOrderUseCase` (transaccional: cart → order → inventory → payment), `ListBuyerOrdersUseCase`, `GetOrderDetailUseCase`, `CancelOrderUseCase`, `ListProducerOrdersUseCase`, `UpdateOrderStatusUseCase`, `ConfirmOrderPaymentUseCase`, `ListOrderStatusHistoryUseCase`, `GenerateInvoiceUseCase`, `GetOrderPaymentDetailsUseCase`.

#### Payments (2)
`ListPaymentMethodsUseCase`, `RegisterManualPaymentProofUseCase`.

#### Reviews (5)
`CreateReviewUseCase`, `ListProductReviewsUseCase`, `ReplyReviewUseCase`, `ListProducerReviewsUseCase`, `ModerateReviewUseCase`.

#### Admin (10)
`ListUsersUseCase`, `BanUserUseCase`, `UnbanUserUseCase`, `ListPendingApprovalsUseCase`, `ApproveProducerUseCase`, `RejectProducerUseCase`, `CreateCategoryUseCase`, `UpdateCategoryUseCase`, `DeleteCategoryUseCase`, `ListAdminActivityUseCase`.

#### Notifications (4)
`ListUserNotificationsUseCase`, `MarkNotificationReadUseCase`, `MarkAllNotificationsReadUseCase`, `EmitNotificationUseCase`.

### 3.3 Ejemplo: `LoginUseCase`

```java
package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.exceptions.UnauthorizedException;
import co.com.marketplace.model.identity.gateways.UserGateway;
import co.com.marketplace.model.identity.gateways.PasswordEncoderGateway;
import co.com.marketplace.model.identity.gateways.TokenProviderGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class LoginUseCase {

    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoder;
    private final TokenProviderGateway tokenProvider;

    public Mono<AuthTokens> execute(String email, String rawPassword) {
        return userGateway.findByEmail(email)
            .switchIfEmpty(Mono.error(new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                "Credenciales inválidas")))
            .flatMap(user -> passwordEncoder.matches(rawPassword, user.getHashedPassword())
                .filter(Boolean::booleanValue)
                .switchIfEmpty(Mono.error(new UnauthorizedException("AUTH_INVALID_CREDENTIALS",
                    "Credenciales inválidas")))
                .then(Mono.just(user)))
            .map(tokenProvider::issueTokens);
    }
}
```

### 3.4 Wiring centralizado

**`applications/app-service/.../config/UseCasesConfig.java`** declara cada use case como `@Bean`:

```java
@Configuration
public class UseCasesConfig {

    @Bean public LoginUseCase loginUseCase(UserGateway u, PasswordEncoderGateway p, TokenProviderGateway t) {
        return new LoginUseCase(u, p, t);
    }
    @Bean public RegisterBuyerUseCase registerBuyerUseCase(UserGateway u, BuyerProfileGateway b,
                                                           CartGateway c, PasswordEncoderGateway p) {
        return new RegisterBuyerUseCase(u, b, c, p);
    }
    // … repetir por cada caso de uso
}
```

### ✅ Checklist FASE 3
- [ ] ~80 casos de uso implementados en `domain/usecase/`
- [ ] Tests unitarios con `StepVerifier` por cada caso de uso
- [ ] `UseCasesConfig` declara todos los beans
- [ ] Cobertura `domain/usecase` > 85% (Jacoco)
- [ ] `./gradlew :usecase:test` pasa
- [ ] **Commit por contexto**: `feat(usecase): add identity use cases`, `feat(usecase): add catalog use cases`, ...

---

## FASE 4 — Adaptadores e Infraestructura

### 4.1 Estructura de `infrastructure/driven-adapters/r2dbc-postgresql/`

```
src/main/java/co/com/marketplace/r2dbc/
├── config/R2dbcConfig.java
├── identity/
│   ├── UserData.java                       (@Table("marketplace.users"))
│   ├── UserReactiveRepository.java         (extends ReactiveCrudRepository<UserData, UUID>)
│   ├── UserRepositoryAdapter.java          (implements UserGateway)
│   └── mapper/UserDataMapper.java          (@Mapper MapStruct)
├── catalog/
│   ├── ProductData.java
│   ├── ProductReactiveRepository.java
│   ├── ProductRepositoryAdapter.java
│   └── mapper/ProductDataMapper.java
├── orders/                                 ...
└── shared/
    ├── helper/ReactiveAdapterOperations.java   (clase base genérica)
    ├── converter/UuidConverter.java
    └── converter/JsonNodeConverter.java
```

**Patrón base** — `ReactiveAdapterOperations<E, D, ID, R extends ReactiveCrudRepository<D,ID>>` provista por el scaffold; cada adapter la extiende.

### 4.2 Convenciones para `*Data.java`

```java
@Table(schema = "marketplace", name = "users")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserData {
    @Id @Column("id")            private UUID id;
    @Column("email")             private String email;
    @Column("hashed_password")   private String hashedPassword;
    @Column("status")            private UserStatusType status;  // ← Java enum, NO String
    @CreatedDate  @Column("created_at") private OffsetDateTime createdAt;
    @LastModifiedDate @Column("updated_at") private OffsetDateTime updatedAt;
}
```

**Regla para columnas ENUM nativo de PostgreSQL**: usar el Java enum de `co.com.marketplace.r2dbc.type` (ej: `UserStatusType`, `OrderStatusType`), **nunca `String`**. El `EnumCodec` registrado en `R2dbcConfig` se encarga de enviar el OID correcto al wire protocol.

**Regla de conversión en adapters** (sin MapStruct — inline):
```java
// toDomain: Java enum → domain enum
.status(UserStatus.valueOf(d.getStatus().name()))

// toData: domain enum → Java enum infra
.status(UserStatusType.valueOf(u.getStatus().name()))

// DatabaseClient.bind: siempre el Java enum (nunca status.name())
spec.bind("status", UserStatusType.valueOf(statusFilter.name()))

// row.get en DatabaseClient: pedir el Java enum
row.get("status", UserStatusType.class)
```

**Columnas VARCHAR con CHECK** (`products.status`, `admin_activity_log.type`, etc.) → siguen usando `String` normalmente.

**Tabla de Java enums de infraestructura** (`co.com.marketplace.r2dbc.type`):

| Java enum | PostgreSQL ENUM | Tablas |
|-----------|----------------|--------|
| `UserStatusType` | `user_status` | `users` |
| `ProducerStatusType` | `producer_status` | `producer_profiles`, `producer_approvals` |
| `OrderStatusType` | `order_status` | `orders`, `order_status_history` |
| `PaymentStatusType` | `payment_status` | `order_payments` |
| `ReviewStatusType` | `review_status` | `reviews` |
| `CouponDiscountType` | `coupon_discount_type` | `coupons` |
| `DocStatusType` | `doc_status` | `producer_documents`, `farm_certifications` |

### 4.3 Driven adapter `security-jwt`

`infrastructure/driven-adapters/security-jwt/`:
- `JwtTokenProvider` implementa `TokenProviderGateway`.
- `BCryptPasswordAdapter` implementa `PasswordEncoderGateway`.

### 4.4 Entry-point `reactive-web`

```
src/main/java/co/com/marketplace/api/
├── config/CorsConfig.java
├── config/SecurityHeadersConfig.java
├── config/SecurityConfig.java                         (SecurityWebFilterChain)
├── config/GlobalErrorWebExceptionHandler.java
├── config/OpenApiConfig.java                          (Swagger)
├── identity/
│   ├── AuthHandler.java
│   ├── AuthRouter.java                                (RouterFunction<ServerResponse>)
│   └── dto/                                           (records: LoginRequest, AuthResponse, ...)
├── catalog/
│   ├── ProductHandler.java
│   ├── CategoryHandler.java
│   └── CatalogRouter.java
├── cart/...
├── orders/...
├── reviews/...
├── producer/                                          (rutas /api/producer/**)
├── admin/                                             (rutas /api/admin/**)
└── shared/
    ├── PagedResponse.java                             (record genérico)
    └── ApiError.java                                  (formato unificado de error)
```

### 4.5 Patrón Handler + Router

**Handler**:

```java
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final LoginUseCase loginUseCase;
    private final Validator validator;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
            .doOnNext(this::validate)
            .flatMap(r -> loginUseCase.execute(r.email(), r.password()))
            .flatMap(tokens -> ServerResponse.ok().bodyValue(AuthResponse.from(tokens)));
    }

    private void validate(LoginRequest r) {
        var violations = validator.validate(r);
        if (!violations.isEmpty()) throw new ValidationException("VALIDATION_ERROR",
            violations.iterator().next().getMessage());
    }
}
```

**Router**:

```java
@Configuration
@RequiredArgsConstructor
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler h) {
        return route()
            .POST("/api/auth/login",                    accept(MediaType.APPLICATION_JSON), h::login)
            .POST("/api/auth/register/buyer",           accept(MediaType.APPLICATION_JSON), h::registerBuyer)
            .POST("/api/auth/register/producer",        accept(MediaType.APPLICATION_JSON), h::registerProducer)
            .POST("/api/auth/refresh",                  accept(MediaType.APPLICATION_JSON), h::refresh)
            .POST("/api/auth/logout",                                                       h::logout)
            .POST("/api/auth/password-reset/request",   accept(MediaType.APPLICATION_JSON), h::requestPasswordReset)
            .POST("/api/auth/password-reset/confirm",   accept(MediaType.APPLICATION_JSON), h::confirmPasswordReset)
            .GET("/api/auth/me",                                                            h::me)
            .build();
    }
}
```

### 4.6 GlobalErrorWebExceptionHandler

Mapea cada `DomainException` a HTTP + body `ApiError` (record):

```java
public record ApiError(String code, String message, OffsetDateTime timestamp, String path) {}
```

### ✅ Checklist FASE 4
- [ ] 12 carpetas de `*Data` + `*ReactiveRepository` + `*RepositoryAdapter` + `*Mapper`
- [ ] `ReactiveAdapterOperations` reutilizado correctamente
- [ ] Conversores UUID/JSON registrados
- [ ] `JwtTokenProvider` y `BCryptPasswordAdapter` implementados
- [ ] Routers + Handlers para los 11 grupos de endpoints (FASE 5)
- [ ] `GlobalErrorWebExceptionHandler` mapea las 5 excepciones de dominio
- [ ] `OpenApiConfig` expone `/swagger-ui.html`
- [ ] **Commit por contexto**: `feat(adapter): r2dbc identity`, `feat(adapter): r2dbc catalog`, ..., `feat(api): auth router`, ...

---

## FASE 5 — Endpoints requeridos por el Frontend

> Convención: prefijo `/api`. Errores siempre en formato `ApiError`. Paginación: `?page=0&size=20&sort=createdAt,desc`. Auth: `Authorization: Bearer <jwt>`.

### 5.1 Identity & Auth (`/api/auth`)

| Método | Ruta | Auth | Request | Response | Códigos |
|--------|------|------|---------|----------|---------|
| POST | `/api/auth/register/buyer` | público | `RegisterBuyerRequest` | `AuthResponse` | 201, 400, 409 |
| POST | `/api/auth/register/producer` | público | `RegisterProducerRequest` | `AuthResponse` | 201, 400, 409 |
| POST | `/api/auth/login` | público | `LoginRequest` | `AuthResponse` | 200, 400, 401 |
| POST | `/api/auth/refresh` | público | `{refreshToken}` | `AuthResponse` | 200, 401 |
| POST | `/api/auth/logout` | usuario | — | 204 | 204, 401 |
| POST | `/api/auth/password-reset/request` | público | `{email}` | 202 | 202 |
| POST | `/api/auth/password-reset/confirm` | público | `{token, newPassword}` | 204 | 204, 400, 410 |
| GET | `/api/auth/me` | usuario | — | `CurrentUserResponse` | 200, 401 |
| PATCH | `/api/auth/me/password` | usuario | `{oldPassword, newPassword}` | 204 | 204, 400, 401 |
| POST | `/api/auth/consents` | usuario | `PrivacyConsentRequest` | 204 | 204 |

### 5.2 Profiles (`/api/profile`)

| Método | Ruta | Auth | Response |
|--------|------|------|----------|
| GET | `/api/profile/buyer` | buyer | `BuyerProfileResponse` |
| PATCH | `/api/profile/buyer` | buyer | `BuyerProfileResponse` |
| GET | `/api/profile/producer` | producer | `ProducerProfileResponse` |
| PATCH | `/api/profile/producer` | producer | `ProducerProfileResponse` |

### 5.3 Addresses (`/api/addresses`)

| Método | Ruta | Auth | Request | Response |
|--------|------|------|---------|----------|
| GET | `/api/addresses` | buyer | — | `List<AddressResponse>` |
| POST | `/api/addresses` | buyer | `AddressRequest` | 201 `AddressResponse` |
| PUT | `/api/addresses/{id}` | buyer | `AddressRequest` | `AddressResponse` |
| DELETE | `/api/addresses/{id}` | buyer | — | 204 |
| PATCH | `/api/addresses/{id}/default` | buyer | — | 204 |

### 5.4 Catalog público (`/api/catalog`)

| Método | Ruta | Query params | Response |
|--------|------|--------------|----------|
| GET | `/api/catalog/products` | `search, category, region, minPrice, maxPrice, certification, roast, page, size, sort` | `PagedResponse<ProductSummary>` |
| GET | `/api/catalog/products/featured` | `limit=8` | `List<ProductSummary>` |
| GET | `/api/catalog/products/{id}` | — | `ProductDetail` |
| GET | `/api/catalog/products/by-slug/{slug}` | — | `ProductDetail` |
| GET | `/api/catalog/products/{id}/reviews` | `page, size` | `PagedResponse<ReviewResponse>` |
| GET | `/api/catalog/categories` | — | `List<CategoryResponse>` |
| GET | `/api/catalog/categories/{slug}` | — | `CategoryDetail` |
| GET | `/api/catalog/certifications` | — | `List<CertificationResponse>` |
| GET | `/api/catalog/roast-levels` | — | `List<RoastLevelResponse>` |

### 5.5 Cart (`/api/cart`) — auth: buyer

| Método | Ruta | Request | Response |
|--------|------|---------|----------|
| GET | `/api/cart` | — | `CartResponse` |
| POST | `/api/cart/items` | `{productId, quantity, presentationId?}` | `CartResponse` |
| PATCH | `/api/cart/items/{itemId}` | `{quantity}` | `CartResponse` |
| DELETE | `/api/cart/items/{itemId}` | — | `CartResponse` |
| DELETE | `/api/cart` | — | 204 |
| POST | `/api/cart/coupon` | `{code}` | `CartResponse` |
| DELETE | `/api/cart/coupon` | — | `CartResponse` |
| PATCH | `/api/cart/shipping` | `{shippingOptionId, addressId}` | `CartResponse` |
| GET | `/api/cart/shipping-options` | — | `List<ShippingOptionResponse>` |

### 5.6 Favorites (`/api/favorites`) — auth: buyer

| Método | Ruta | Response |
|--------|------|----------|
| GET | `/api/favorites` | `List<ProductSummary>` |
| POST | `/api/favorites/{productId}` | 201 |
| DELETE | `/api/favorites/{productId}` | 204 |

### 5.7 Orders (`/api/orders`)

| Método | Ruta | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/api/orders` | buyer | `PlaceOrderRequest` (addressId, paymentMethodId, notes?) | 201 `OrderDetailResponse` |
| GET | `/api/orders` | buyer | query: `status, page, size` | `PagedResponse<OrderSummary>` |
| GET | `/api/orders/{id}` | buyer/producer/admin | — | `OrderDetailResponse` |
| POST | `/api/orders/{id}/cancel` | buyer | `{reason}` | `OrderDetailResponse` |
| GET | `/api/orders/{id}/timeline` | buyer/producer | — | `List<OrderStatusEntry>` |
| GET | `/api/orders/{id}/invoice` | buyer | — | `InvoiceResponse` |
| POST | `/api/orders/{id}/payment-proof` | buyer | `{reference, screenshotUrl?, amount}` | `OrderPaymentResponse` |
| GET | `/api/orders/{id}/payment` | buyer/producer | — | `OrderPaymentResponse` |

### 5.8 Payment Methods (`/api/payment-methods`)

| Método | Ruta | Auth | Response |
|--------|------|------|----------|
| GET | `/api/payment-methods` | usuario | `List<PaymentMethodResponse>` |

### 5.9 Reviews (`/api/reviews`)

| Método | Ruta | Auth | Request | Response |
|--------|------|------|---------|----------|
| POST | `/api/reviews` | buyer | `{productId, orderId, rating, title, body}` | 201 `ReviewResponse` |
| POST | `/api/reviews/{id}/reply` | producer/admin | `{body}` | 201 `ReviewReplyResponse` |
| PATCH | `/api/reviews/{id}/moderate` | admin | `{action, reason?}` | `ReviewResponse` |

### 5.10 Producer (`/api/producer/**`) — auth: producer

| Método | Ruta | Request | Response |
|--------|------|---------|----------|
| GET | `/api/producer/products` | query: `status, page, size` | `PagedResponse<ProductSummary>` |
| POST | `/api/producer/products` | `CreateProductRequest` | 201 `ProductDetail` |
| PUT | `/api/producer/products/{id}` | `UpdateProductRequest` | `ProductDetail` |
| POST | `/api/producer/products/{id}/archive` | — | 204 |
| POST | `/api/producer/products/{id}/images` | multipart | `ProductImageResponse` |
| GET | `/api/producer/orders` | query: `status, page, size` | `PagedResponse<OrderSummary>` |
| PATCH | `/api/producer/orders/{id}/status` | `{newStatus, note?}` | `OrderDetailResponse` |
| POST | `/api/producer/orders/{id}/payment/confirm` | `{verified, note?}` | `OrderPaymentResponse` |
| GET | `/api/producer/farm` | — | `FarmResponse` |
| PATCH | `/api/producer/farm` | `FarmRequest` | `FarmResponse` |
| GET | `/api/producer/reviews` | query: `page, size` | `PagedResponse<ReviewResponse>` |
| GET | `/api/producer/inventory` | query: `productId?` | `List<InventoryItemResponse>` |
| POST | `/api/producer/inventory/adjust` | `InventoryAdjustmentRequest` | `InventoryItemResponse` |

### 5.11 Admin (`/api/admin/**`) — auth: admin

| Método | Ruta | Request | Response |
|--------|------|---------|----------|
| GET | `/api/admin/users` | query: `role, status, search, page, size` | `PagedResponse<UserSummary>` |
| PATCH | `/api/admin/users/{id}/ban` | `{reason}` | `UserSummary` |
| PATCH | `/api/admin/users/{id}/unban` | — | `UserSummary` |
| GET | `/api/admin/producer-approvals` | query: `status, page, size` | `PagedResponse<ProducerApprovalResponse>` |
| GET | `/api/admin/producer-approvals/{id}` | — | `ProducerApprovalDetail` |
| PATCH | `/api/admin/producer-approvals/{id}/approve` | `{notes?}` | `ProducerApprovalResponse` |
| PATCH | `/api/admin/producer-approvals/{id}/reject` | `{reason}` | `ProducerApprovalResponse` |
| GET | `/api/admin/categories` | — | `List<CategoryResponse>` |
| POST | `/api/admin/categories` | `CategoryRequest` | 201 `CategoryResponse` |
| PUT | `/api/admin/categories/{id}` | `CategoryRequest` | `CategoryResponse` |
| DELETE | `/api/admin/categories/{id}` | — | 204 |
| GET | `/api/admin/activity` | query: `actorId?, action?, from?, to?, page, size` | `PagedResponse<ActivityLogEntry>` |
| GET | `/api/admin/dashboard/stats` | — | `AdminStatsResponse` |

### 5.12 Notifications (`/api/notifications`) — auth: usuario

| Método | Ruta | Response |
|--------|------|----------|
| GET | `/api/notifications` | `PagedResponse<NotificationResponse>` |
| PATCH | `/api/notifications/{id}/read` | `NotificationResponse` |
| POST | `/api/notifications/read-all` | 204 |

**Total endpoints: ≈ 95.**

### ✅ Checklist FASE 5
- [ ] 12 routers funcionando (`identity, profile, address, catalog, cart, favorites, order, payment-method, review, producer, admin, notification`)
- [ ] Cada endpoint validado con OpenAPI/Swagger
- [ ] Reglas de seguridad por rol aplicadas en `SecurityConfig`
- [ ] Pruebas de contrato (`@WebFluxTest`) por handler
- [ ] **Commit por bloque de routers**: `feat(api): expose auth endpoints`, `feat(api): expose catalog endpoints`, ...

---

## FASE 6 — Integración Front ↔ Back

### 6.1 Variables de entorno frontend

**`marketplace-cafe-front/src/environments/environment.ts`**:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

**`environment.production.ts`**:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://api.wcm.co/api'
};
```

### 6.2 Interceptores HTTP a verificar / crear

- `BaseUrlInterceptor` (ya existe): antepone `environment.apiUrl`.
- `AuthTokenInterceptor` (crear): inyecta `Authorization: Bearer ${token}` desde `TokenStorageService`.
- `ErrorInterceptor` (crear): mapea `ApiError` → `NotificationService.error()`.

### 6.3 Orden de migración recomendado (por bounded context backend)

| Orden | Bounded context | Servicios Angular a migrar | Pantalla afectada |
|-------|-----------------|----------------------------|-------------------|
| 1 | Identity | `AuthService`, `TokenStorageService` | Login, registro, recuperación |
| 2 | Identity-Address | `AddressService`, `BuyerProfileService`, `ProducerProfileService` | Perfil, checkout |
| 3 | Catalog | `ProductService`, `CategoryService` | Landing, detalle |
| 4 | Reviews | `ReviewService`, `ProducerReviewService` | Detalle, panel productor |
| 5 | Cart | `CartService` | Carrito |
| 6 | Favorites | `FavoritesService` | Mis favoritos |
| 7 | Orders | `OrderService` | Mis pedidos, checkout |
| 8 | Producer Catalog | `ProducerProductService`, `FarmService` | Panel productor |
| 9 | Producer Orders | `ProducerOrderService` | Pedidos del productor |
| 10 | Admin | `AdminUserService`, `ProducerApprovalService`, `AdminCategoryService`, `AdminActivityService` | Panel admin |
| 11 | Cleanup | eliminar `SEED_*` constants | — |

**Regla**: cada migración usa una **rama feature/integration-X**, debe pasar `npm run build` y validarse manualmente antes de mergear.

### 6.4 Patrón de servicio Angular migrado (ejemplo)

```typescript
// auth.service.ts (después de migración)
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '@env/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  readonly currentUser = signal<CurrentUser | null>(null);

  login(payload: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`/auth/login`, payload).pipe(
      tap(res => {
        this.tokenStorage.save(res.accessToken, res.refreshToken);
        this.currentUser.set(res.user);
      })
    );
  }
}
```

### ✅ Checklist FASE 6
- [ ] `environment.ts` apunta a `http://localhost:8080/api`
- [ ] 3 interceptores activos en `app.config.ts`
- [ ] Cada uno de los 11 servicios migrado y verificado en navegador
- [ ] Tests E2E mínimos: login, agregar al carrito, checkout, panel productor
- [ ] `npm run build` pasa sin errores
- [ ] **Commit en frontend por servicio**: `feat: migrate AuthService to backend HTTP`

---

## FASE 7 — Pruebas y Validación

### 7.1 Pruebas unitarias por capa

| Capa | Framework | Cobertura objetivo |
|------|-----------|--------------------|
| `domain/model` | JUnit 5 | 70% |
| `domain/usecase` | JUnit 5 + Mockito + StepVerifier | 85% |
| `infrastructure/driven-adapters/r2dbc-postgresql` | `@DataR2dbcTest` + Testcontainers | 70% |
| `infrastructure/entry-points/reactive-web` | `@WebFluxTest` + Mockito | 80% |

### 7.2 Pruebas de integración

**Testcontainers PostgreSQL** ejecuta `db_schema_v3.sql` en cada arranque.

```java
@SpringBootTest
@AutoConfigureWebTestClient
@Testcontainers
class OrderFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("wcm_test").withUsername("wcm").withPassword("wcm");

    @Test void placeOrderEndToEnd() { /* ... */ }
}
```

### 7.3 Pruebas E2E con frontend

`docker-compose up --build` levanta `postgres + backend + frontend`. Flujos a verificar:

1. Registro de comprador → login → ver productos.
2. Agregar al carrito → aplicar cupón → checkout → orden creada.
3. Productor crea producto → comprador lo ve en catálogo → lo compra.
4. Admin aprueba productor pendiente.

### 7.4 Pruebas de mutación (Pitest)

`./gradlew pitest` — objetivo: ≥ 60% de mutaciones detectadas en `domain/usecase`.

### 7.5 Pruebas de carga (opcional)

`./gradlew gatlingRun` con escenario "100 usuarios concurrentes navegan catálogo".

### ✅ Checklist FASE 7
- [ ] Cobertura global Jacoco ≥ 80%
- [ ] Mutaciones Pitest ≥ 60% en use cases
- [ ] 5 flujos E2E pasan con `docker-compose up`
- [ ] Swagger documenta los ~95 endpoints
- [ ] `./gradlew build` 100% verde en CI
- [ ] **Commit final**: `chore: backend feature-complete and integrated`
- [ ] **Tag**: `git tag -a v1.0.0 -m "Backend integrado con frontend"`

---

## CONVENCIONES GLOBALES

### Estructura de paquetes

```
co.com.marketplace
├── model.{contexto}                    (domain/model)
├── model.{contexto}.gateways
├── model.exceptions
├── usecase.{contexto}                  (domain/usecase)
├── r2dbc.{contexto}                    (driven-adapters/r2dbc-postgresql)
├── jwt                                 (driven-adapters/security-jwt)
├── api.{contexto}                      (entry-points/reactive-web)
├── api.config
├── api.shared
└── config                              (applications/app-service)
```

### Reglas inviolables

1. **Estructura del scaffold Bancolombia no se modifica**.
2. **Domain no depende de Spring** (verificado por `ArchitectureTest`).
3. **Sin código bloqueante**: prohibido `.block()`, `Thread.sleep()`, JDBC. Solo `Mono`/`Flux`.
4. **SOLID**:
   - SRP: una clase, una razón para cambiar.
   - OCP: extender vía nuevos use cases, no modificar existentes.
   - LSP: gateways reactivos respetan contratos.
   - ISP: gateways pequeños y específicos.
   - DIP: use cases dependen de gateways (interfaces), no de adapters.
5. **Java 21**: usar records, sealed classes, pattern matching `switch`, virtual threads N/A (somos reactivos).
6. **Logs estructurados**: `Slf4j` + JSON en producción.
7. **Errores**: lanzar `DomainException`, dejar que `GlobalErrorWebExceptionHandler` mapee.
8. **Idempotencia**: endpoints `POST /orders` aceptan header `Idempotency-Key`.
9. **Auditoría**: `@CreatedDate`/`@LastModifiedDate` en todas las entidades persistidas.
10. **Versionado**: la API es **`/api/v1/...`** desde el principio (omitido aquí por brevedad — agregar prefijo en el `RouterFunction`).

### Comandos diarios

```bash
./gradlew build                      # build completo
./gradlew :app-service:bootRun       # arrancar backend
./gradlew test                       # tests unitarios
./gradlew jacocoMergedReport         # reporte de cobertura
./gradlew pitest                     # mutaciones
./gradlew :app-service:integrationTest  # tests Testcontainers
docker-compose up postgres -d        # solo BD
docker-compose up --build            # stack completo
```

### Estado vivo

`memory/BACKEND_STATE.md` se actualiza al final de cada sesión Claude Code con: fase actual, decisiones, problemas conocidos.

---

**Fin del documento.** Cualquier ambigüedad debe resolverse contra:
1. `db_schema_v3.sql` (fuente de verdad del modelo de datos).
2. Código del frontend en `marketplace-cafe-front/src/app/` (fuente de verdad de los endpoints requeridos).
3. ADRs en `marketplace-cafe-front/md/anexos/ADR_WCM_Completo.docx`.
