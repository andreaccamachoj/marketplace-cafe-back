package co.com.marketplace.r2dbc.admin;

import co.com.marketplace.r2dbc.type.ProducerStatusType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProducerApprovalReactiveRepository extends ReactiveCrudRepository<ProducerApprovalData, UUID> {
    @Query("SELECT * FROM marketplace.producer_approvals WHERE producer_id = :producerId")
    Mono<ProducerApprovalData> findByProducerId(UUID producerId);

    @Query("SELECT * FROM marketplace.producer_approvals WHERE status = :status ORDER BY submitted_at DESC LIMIT :size OFFSET :offset")
    Flux<ProducerApprovalData> findByStatus(ProducerStatusType status, int size, long offset);

    @Query("SELECT COUNT(*) FROM marketplace.producer_approvals WHERE status = :status")
    Mono<Long> countByStatus(ProducerStatusType status);

    @Query("SELECT * FROM marketplace.producer_approvals ORDER BY submitted_at DESC LIMIT :size OFFSET :offset")
    Flux<ProducerApprovalData> findAllPaged(int size, long offset);
}
