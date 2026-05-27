package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.r2dbc.type.ProducerStatusType;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProducerProfileReactiveRepository extends ReactiveCrudRepository<ProducerProfileData, UUID> {
    Mono<ProducerProfileData> findByUserId(UUID userId);

    @Query("SELECT * FROM marketplace.producer_profiles WHERE status = :status LIMIT :size OFFSET :offset")
    Flux<ProducerProfileData> findByStatus(ProducerStatusType status, int size, long offset);

    @Query("SELECT COUNT(*) FROM marketplace.producer_profiles WHERE status = :status")
    Mono<Long> countByStatus(ProducerStatusType status);
}
