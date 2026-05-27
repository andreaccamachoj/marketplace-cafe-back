package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.r2dbc.type.ProducerStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProducerProfileRepositoryAdapterTest {

    @Mock private ProducerProfileReactiveRepository repository;
    @Mock private R2dbcEntityTemplate template;

    @InjectMocks private ProducerProfileRepositoryAdapter adapter;

    private final UUID profileId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private ProducerProfileData profileData;
    private ProducerProfile profile;

    @BeforeEach
    void setUp() {
        profileData = ProducerProfileData.builder()
                .id(profileId)
                .userId(userId)
                .bio("Bio del productor")
                .city("Manizales")
                .department("Caldas")
                .status(ProducerStatusType.pending)
                .avatarInitials("PP")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        profile = ProducerProfile.builder()
                .id(profileId)
                .userId(userId)
                .bio("Bio del productor")
                .city("Manizales")
                .department("Caldas")
                .status(ProducerStatus.pending)
                .avatarInitials("PP")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsProfile_whenSuccessful() {
        when(repository.save(any(ProducerProfileData.class))).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.save(profile))
                .expectNextMatches(p -> profileId.equals(p.getId()) && "Manizales".equals(p.getCity()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsProfile_whenFound() {
        when(repository.findByUserId(userId)).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNextMatches(p -> userId.equals(p.getUserId()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNotFound() {
        when(repository.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyComplete();
    }

    @Test
    void findById_returnsProfile_whenFound() {
        when(repository.findById(profileId)).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.findById(profileId))
                .expectNextMatches(p -> profileId.equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void update_returnsUpdatedProfile_whenSuccessful() {
        when(template.update(any(ProducerProfileData.class))).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.update(profile))
                .expectNextMatches(p -> profileId.equals(p.getId()))
                .verifyComplete();
    }

    @Test
    void findByStatus_returnsList_whenFound() {
        when(repository.findByStatus(ProducerStatusType.pending, 10, 0L))
                .thenReturn(Flux.just(profileData));

        StepVerifier.create(adapter.findByStatus(ProducerStatus.pending, 0, 10))
                .expectNextMatches(p -> ProducerStatus.pending.equals(p.getStatus()))
                .verifyComplete();
    }

    @Test
    void countByStatus_returnsCount() {
        when(repository.countByStatus(ProducerStatusType.pending)).thenReturn(Mono.just(3L));

        StepVerifier.create(adapter.countByStatus(ProducerStatus.pending))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void save_propagatesError() {
        when(repository.save(any(ProducerProfileData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(profile))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByUserId_propagatesError() {
        when(repository.findByUserId(userId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findById_propagatesError() {
        when(repository.findById(profileId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findById(profileId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void update_propagatesError() {
        when(template.update(any(ProducerProfileData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.update(profile))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByStatus_propagatesError() {
        when(repository.findByStatus(ProducerStatusType.pending, 10, 0L))
                .thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByStatus(ProducerStatus.pending, 0, 10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void countByStatus_propagatesError() {
        when(repository.countByStatus(ProducerStatusType.pending)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.countByStatus(ProducerStatus.pending))
                .verifyError(RuntimeException.class);
    }
}
