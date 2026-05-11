package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.BuyerProfile;
import co.com.marketplace.model.identity.gateways.BuyerProfileGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyerProfileRepositoryAdapter implements BuyerProfileGateway {

    private final BuyerProfileReactiveRepository repository;
    private final R2dbcEntityTemplate template;

    private static BuyerProfile toDomain(BuyerProfileData d) {
        return BuyerProfile.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .city(d.getCity())
                .department(d.getDepartment())
                .preferredPayment(d.getPreferredPayment())
                .newsletterOptIn(d.isNewsletterOptIn())
                .avatarInitials(d.getAvatarInitials())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private static BuyerProfileData toData(BuyerProfile p) {
        return BuyerProfileData.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .city(p.getCity())
                .department(p.getDepartment())
                .preferredPayment(p.getPreferredPayment())
                .newsletterOptIn(p.isNewsletterOptIn())
                .avatarInitials(p.getAvatarInitials())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    public Mono<BuyerProfile> save(BuyerProfile profile) {
        return repository.save(toData(profile))
                .doOnSubscribe(s -> log.debug("[BuyerProfileRepositoryAdapter#save] DB request: userId={}", profile.getUserId()))
                .doOnSuccess(r -> log.debug("[BuyerProfileRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[BuyerProfileRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(BuyerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<BuyerProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .doOnSubscribe(s -> log.debug("[BuyerProfileRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnSuccess(r -> log.debug("[BuyerProfileRepositoryAdapter#findByUserId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[BuyerProfileRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(BuyerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<BuyerProfile> update(BuyerProfile profile) {
        return template.update(toData(profile))
                .doOnSubscribe(s -> log.debug("[BuyerProfileRepositoryAdapter#update] DB request: userId={}", profile.getUserId()))
                .doOnSuccess(r -> log.debug("[BuyerProfileRepositoryAdapter#update] DB response: result={}", r != null))
                .doOnError(e -> log.error("[BuyerProfileRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .map(BuyerProfileRepositoryAdapter::toDomain);
    }
}
