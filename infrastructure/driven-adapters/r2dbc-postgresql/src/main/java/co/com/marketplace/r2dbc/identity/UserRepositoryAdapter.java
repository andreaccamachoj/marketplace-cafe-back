package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.UserGateway;
import co.com.marketplace.r2dbc.type.UserStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserGateway {

    private final UserReactiveRepository repository;
    private final DatabaseClient databaseClient;

    private static User toDomain(UserData d) {
        return User.builder()
                .id(d.getId())
                .email(d.getEmail())
                .hashedPassword(d.getPasswordHash())
                .fullName(d.getFullName())
                .phone(d.getPhone())
                .status(UserStatus.valueOf(d.getStatus().name()))
                .privacyConsent(d.isPrivacyConsent())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private static UserData toData(User u) {
        return UserData.builder()
                .id(u.getId())
                .email(u.getEmail())
                .passwordHash(u.getHashedPassword())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .status(UserStatusType.valueOf(u.getStatus().name()))
                .privacyConsent(u.isPrivacyConsent())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }

    @Override
    public Mono<User> save(User user) {
        return repository.save(toData(user))
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#save] DB request: email={}", user.getEmail()))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[UserRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<User> findById(UUID id) {
        return repository.findById(id)
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[UserRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#findByEmail] DB request: email={}", email))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#findByEmail] DB response: found={}", r != null))
                .doOnError(e -> log.error("[UserRepositoryAdapter#findByEmail] DB error: {}", e.getMessage()))
                .map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return repository.existsByEmail(email)
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#existsByEmail] DB request: email={}", email))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#existsByEmail] DB response: result={}", r))
                .doOnError(e -> log.error("[UserRepositoryAdapter#existsByEmail] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<User> update(User user) {
        return repository.save(toData(user))
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#update] DB request: id={}", user.getId()))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#update] DB response: result={}", r != null))
                .doOnError(e -> log.error("[UserRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id)
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#deleteById] DB request: id={}", id))
                .doOnTerminate(() -> log.debug("[UserRepositoryAdapter#deleteById] DB response: done"))
                .doOnError(e -> log.error("[UserRepositoryAdapter#deleteById] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<User> findAll(String roleFilter, UserStatus statusFilter, String search, int page, int size) {
        StringBuilder sql = new StringBuilder(
                "SELECT DISTINCT u.*, r.name AS role_name FROM marketplace.users u" +
                " LEFT JOIN marketplace.user_roles ur ON u.id = ur.user_id" +
                " LEFT JOIN marketplace.roles r ON r.id = ur.role_id");
        sql.append(" WHERE 1=1");
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append(" AND r.name = :roleFilter");
        }
        if (statusFilter != null) {
            sql.append(" AND u.status = CAST(:statusFilter AS marketplace.user_status)");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.email ILIKE :search OR u.full_name ILIKE :search)");
        }
        sql.append(" ORDER BY u.created_at DESC LIMIT :size OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (roleFilter != null && !roleFilter.isBlank()) {
            spec = spec.bind("roleFilter", roleFilter);
        }
        if (statusFilter != null) {
            spec = spec.bind("statusFilter", statusFilter.name());
        }
        if (search != null && !search.isBlank()) {
            spec = spec.bind("search", "%" + search + "%");
        }
        spec = spec.bind("size", size).bind("offset", (long) page * size);

        return spec.map((row, meta) -> User.builder()
                        .id(row.get("id", UUID.class))
                        .email(row.get("email", String.class))
                        .hashedPassword(row.get("password_hash", String.class))
                        .fullName(row.get("full_name", String.class))
                        .phone(row.get("phone", String.class))
                        .status(UserStatus.valueOf(UserStatusType.valueOf(row.get("status", String.class)).name()))
                        .privacyConsent(Boolean.TRUE.equals(row.get("privacy_consent", Boolean.class)))
                        .createdAt(row.get("created_at", java.time.OffsetDateTime.class))
                        .updatedAt(row.get("updated_at", java.time.OffsetDateTime.class))
                        .role(row.get("role_name", String.class))
                        .build())
                .all()
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#findAll] DB request: page={}, size={}, roleFilter={}", page, size, roleFilter))
                .doOnComplete(() -> log.debug("[UserRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[UserRepositoryAdapter#findAll] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Long> countAll(String roleFilter, UserStatus statusFilter, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT u.id) FROM marketplace.users u");
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append(" JOIN marketplace.user_roles ur ON u.id = ur.user_id")
               .append(" JOIN marketplace.roles r ON r.id = ur.role_id");
        }
        sql.append(" WHERE 1=1");
        if (roleFilter != null && !roleFilter.isBlank()) {
            sql.append(" AND r.name = :roleFilter");
        }
        if (statusFilter != null) {
            sql.append(" AND u.status = :statusFilter");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (u.email ILIKE :search OR u.full_name ILIKE :search)");
        }

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        if (roleFilter != null && !roleFilter.isBlank()) {
            spec = spec.bind("roleFilter", roleFilter);
        }
        if (statusFilter != null) {
            spec = spec.bind("statusFilter", UserStatusType.valueOf(statusFilter.name()));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.bind("search", "%" + search + "%");
        }

        return spec.map((row, meta) -> row.get(0, Long.class)).one()
                .doOnSubscribe(s -> log.debug("[UserRepositoryAdapter#countAll] DB request: roleFilter={}, statusFilter={}", roleFilter, statusFilter))
                .doOnSuccess(r -> log.debug("[UserRepositoryAdapter#countAll] DB response: result={}", r))
                .doOnError(e -> log.error("[UserRepositoryAdapter#countAll] DB error: {}", e.getMessage()));
    }
}
