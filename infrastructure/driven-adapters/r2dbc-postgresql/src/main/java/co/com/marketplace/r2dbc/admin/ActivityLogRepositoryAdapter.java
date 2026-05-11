package co.com.marketplace.r2dbc.admin;

import co.com.marketplace.model.admin.AdminActivityLog;
import co.com.marketplace.model.admin.gateways.ActivityLogGateway;
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
public class ActivityLogRepositoryAdapter implements ActivityLogGateway {

    private final AdminActivityLogReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<AdminActivityLog> save(AdminActivityLog activityLog) {
        return repo.save(toData(activityLog))
                .doOnSubscribe(s -> log.debug("[ActivityLogRepositoryAdapter#save] DB request: actorId={}", activityLog.getActorId()))
                .doOnSuccess(r -> log.debug("[ActivityLogRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ActivityLogRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<AdminActivityLog> findAll(UUID actorId, String action, OffsetDateTime from, OffsetDateTime to, int page, int size) {
        StringBuilder sql = new StringBuilder("SELECT * FROM marketplace.admin_activity_log WHERE 1=1");
        if (actorId != null) sql.append(" AND actor_id = :actorId");
        if (action != null) sql.append(" AND type = :action");
        if (from != null) sql.append(" AND created_at >= :from");
        if (to != null) sql.append(" AND created_at <= :to");
        sql.append(" ORDER BY created_at DESC LIMIT :size OFFSET :offset");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString())
                .bind("size", size)
                .bind("offset", (long) page * size);
        if (actorId != null) spec = spec.bind("actorId", actorId);
        if (action != null) spec = spec.bind("action", action);
        if (from != null) spec = spec.bind("from", from);
        if (to != null) spec = spec.bind("to", to);

        return spec.map((row, meta) -> AdminActivityLogData.builder()
                        .id(row.get("id", UUID.class))
                        .actorId(row.get("actor_id", UUID.class))
                        .actorNameSnapshot(row.get("actor_name_snapshot", String.class))
                        .type(row.get("type", String.class))
                        .title(row.get("title", String.class))
                        .description(row.get("description", String.class))
                        .severity(row.get("severity", String.class))
                        .iconEmoji(row.get("icon_emoji", String.class))
                        .metadata(row.get("metadata", String.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .build())
                .all()
                .doOnSubscribe(s -> log.debug("[ActivityLogRepositoryAdapter#findAll] DB request: page={}, size={}", page, size))
                .doOnComplete(() -> log.debug("[ActivityLogRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[ActivityLogRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countAll(UUID actorId, String action, OffsetDateTime from, OffsetDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM marketplace.admin_activity_log WHERE 1=1");
        if (actorId != null) sql.append(" AND actor_id = :actorId");
        if (action != null) sql.append(" AND type = :action");
        if (from != null) sql.append(" AND created_at >= :from");
        if (to != null) sql.append(" AND created_at <= :to");

        DatabaseClient.GenericExecuteSpec spec = db.sql(sql.toString());
        if (actorId != null) spec = spec.bind("actorId", actorId);
        if (action != null) spec = spec.bind("action", action);
        if (from != null) spec = spec.bind("from", from);
        if (to != null) spec = spec.bind("to", to);

        return spec.map((row, meta) -> row.get(0, Long.class)).one()
                .doOnSubscribe(s -> log.debug("[ActivityLogRepositoryAdapter#countAll] DB request: actorId={}", actorId))
                .doOnSuccess(r -> log.debug("[ActivityLogRepositoryAdapter#countAll] DB response: result={}", r))
                .doOnError(e -> log.error("[ActivityLogRepositoryAdapter#countAll] DB error: {}", e.getMessage()));
    }

    private AdminActivityLog toDomain(AdminActivityLogData d) {
        return AdminActivityLog.builder()
                .id(d.getId())
                .actorId(d.getActorId())
                .actorNameSnapshot(d.getActorNameSnapshot())
                .type(d.getType())
                .title(d.getTitle())
                .description(d.getDescription())
                .severity(d.getSeverity())
                .iconEmoji(d.getIconEmoji())
                .metadata(d.getMetadata())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private AdminActivityLogData toData(AdminActivityLog l) {
        return AdminActivityLogData.builder()
                .id(l.getId())
                .actorId(l.getActorId())
                .actorNameSnapshot(l.getActorNameSnapshot())
                .type(l.getType())
                .title(l.getTitle())
                .description(l.getDescription())
                .severity(l.getSeverity())
                .iconEmoji(l.getIconEmoji())
                .metadata(l.getMetadata())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
