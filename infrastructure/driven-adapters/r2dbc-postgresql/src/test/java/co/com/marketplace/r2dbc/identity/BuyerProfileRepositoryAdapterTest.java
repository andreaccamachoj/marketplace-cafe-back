package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.BuyerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyerProfileRepositoryAdapterTest {

    @Mock private BuyerProfileReactiveRepository repository;
    @Mock private R2dbcEntityTemplate template;

    @InjectMocks private BuyerProfileRepositoryAdapter adapter;

    private final UUID profileId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private BuyerProfileData profileData;
    private BuyerProfile profile;

    @BeforeEach
    void setUp() {
        profileData = BuyerProfileData.builder()
                .id(profileId)
                .userId(userId)
                .city("Bogotá")
                .department("Cundinamarca")
                .preferredPayment("transferencia")
                .newsletterOptIn(true)
                .avatarInitials("TU")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        profile = BuyerProfile.builder()
                .id(profileId)
                .userId(userId)
                .city("Bogotá")
                .department("Cundinamarca")
                .preferredPayment("transferencia")
                .newsletterOptIn(true)
                .avatarInitials("TU")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsProfile_whenSuccessful() {
        when(repository.save(any(BuyerProfileData.class))).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.save(profile))
                .expectNextMatches(p -> profileId.equals(p.getId()) && "Bogotá".equals(p.getCity()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repository.save(any(BuyerProfileData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(profile))
                .verifyError(RuntimeException.class);
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
    void update_returnsUpdatedProfile_whenSuccessful() {
        when(template.update(any(BuyerProfileData.class))).thenReturn(Mono.just(profileData));

        StepVerifier.create(adapter.update(profile))
                .expectNextMatches(p -> profileId.equals(p.getId()))
                .verifyComplete();
    }
}
