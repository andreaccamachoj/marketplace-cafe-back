package co.com.marketplace.r2dbc.admin;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface AdminActivityLogReactiveRepository extends ReactiveCrudRepository<AdminActivityLogData, UUID> {
}
