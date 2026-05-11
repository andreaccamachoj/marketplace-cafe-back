package co.com.marketplace.r2dbc.farm;

import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.shared.DocStatus;
import co.com.marketplace.r2dbc.type.DocStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FarmRepositoryAdapter implements FarmGateway {

    private final FarmReactiveRepository farmRepository;
    private final FarmCertificationReactiveRepository certRepository;
    private final R2dbcEntityTemplate template;

    @Override
    public Mono<Farm> save(Farm farm) {
        return farmRepository.save(toData(farm))
                .doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#save] DB request: producerId={}", farm.getProducerId()))
                .doOnSuccess(r -> log.debug("[FarmRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(FarmRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Farm> findByProducerId(UUID producerId) {
        return farmRepository.findByProducerId(producerId)
                .doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#findByProducerId] DB request: producerId={}", producerId))
                .doOnSuccess(r -> log.debug("[FarmRepositoryAdapter#findByProducerId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(FarmRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Farm> update(Farm farm) {
        return template.update(
                Query.query(Criteria.where("id").is(farm.getId())),
                Update.update("name", farm.getName())
                        .set("municipality", farm.getMunicipality())
                        .set("department", farm.getDepartment())
                        .set("altitude_masl", farm.getAltitudeMasl())
                        .set("area_hectares", farm.getAreaHectares())
                        .set("main_variety", farm.getMainVariety())
                        .set("process", farm.getProcess())
                        .set("tree_count", farm.getTreeCount())
                        .set("harvest_season", farm.getHarvestSeason())
                        .set("annual_production_sacos", farm.getAnnualProductionSacos())
                        .set("yield_per_ha", farm.getYieldPerHa())
                        .set("cupping_score", farm.getCuppingScore())
                        .set("description", farm.getDescription())
                        .set("updated_at", OffsetDateTime.now()),
                FarmData.class
        ).doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#update] DB request: id={}", farm.getId()))
                .doOnSuccess(r -> log.debug("[FarmRepositoryAdapter#update] DB response: result={}", r))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .then(farmRepository.findById(farm.getId()).map(FarmRepositoryAdapter::toDomain));
    }

    @Override
    public Mono<FarmCertification> saveCertification(FarmCertification certification) {
        return certRepository.save(certToData(certification))
                .doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#saveCertification] DB request: farmId={}", certification.getFarmId()))
                .doOnSuccess(r -> log.debug("[FarmRepositoryAdapter#saveCertification] DB response: result={}", r != null))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#saveCertification] DB error: {}", e.getMessage()))
                .map(FarmRepositoryAdapter::certToDomain);
    }

    @Override
    public Mono<Void> deleteCertification(UUID certificationId) {
        return certRepository.deleteById(certificationId)
                .doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#deleteCertification] DB request: id={}", certificationId))
                .doOnTerminate(() -> log.debug("[FarmRepositoryAdapter#deleteCertification] DB response: done"))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#deleteCertification] DB error: {}", e.getMessage()));
    }

    @Override
    public Flux<FarmCertification> findCertificationsByFarmId(UUID farmId) {
        return certRepository.findByFarmId(farmId)
                .doOnSubscribe(s -> log.debug("[FarmRepositoryAdapter#findCertificationsByFarmId] DB request: farmId={}", farmId))
                .doOnComplete(() -> log.debug("[FarmRepositoryAdapter#findCertificationsByFarmId] DB response: complete"))
                .doOnError(e -> log.error("[FarmRepositoryAdapter#findCertificationsByFarmId] DB error: {}", e.getMessage()))
                .map(FarmRepositoryAdapter::certToDomain);
    }

    static Farm toDomain(FarmData d) {
        return Farm.builder()
                .id(d.getId())
                .producerId(d.getProducerId())
                .name(d.getName())
                .municipality(d.getMunicipality())
                .department(d.getDepartment())
                .altitudeMasl(d.getAltitudeMasl())
                .areaHectares(d.getAreaHectares())
                .mainVariety(d.getMainVariety())
                .process(d.getProcess())
                .treeCount(d.getTreeCount())
                .harvestSeason(d.getHarvestSeason())
                .annualProductionSacos(d.getAnnualProductionSacos())
                .yieldPerHa(d.getYieldPerHa())
                .cuppingScore(d.getCuppingScore())
                .description(d.getDescription())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    static FarmData toData(Farm f) {
        return FarmData.builder()
                .id(f.getId())
                .producerId(f.getProducerId())
                .name(f.getName())
                .municipality(f.getMunicipality())
                .department(f.getDepartment())
                .altitudeMasl(f.getAltitudeMasl())
                .areaHectares(f.getAreaHectares())
                .mainVariety(f.getMainVariety())
                .process(f.getProcess())
                .treeCount(f.getTreeCount())
                .harvestSeason(f.getHarvestSeason())
                .annualProductionSacos(f.getAnnualProductionSacos())
                .yieldPerHa(f.getYieldPerHa())
                .cuppingScore(f.getCuppingScore())
                .description(f.getDescription())
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .build();
    }

    static FarmCertification certToDomain(FarmCertificationData d) {
        return FarmCertification.builder()
                .id(d.getId())
                .farmId(d.getFarmId())
                .certificationId(d.getCertificationId())
                .issuer(d.getIssuer())
                .issueDate(d.getIssueDate())
                .expiryDate(d.getExpiryDate())
                .status(d.getStatus() != null ? DocStatus.valueOf(d.getStatus().name()) : null)
                .documentUrl(d.getDocumentUrl())
                .notes(d.getNotes())
                .build();
    }

    static FarmCertificationData certToData(FarmCertification fc) {
        return FarmCertificationData.builder()
                .id(fc.getId())
                .farmId(fc.getFarmId())
                .certificationId(fc.getCertificationId())
                .issuer(fc.getIssuer())
                .issueDate(fc.getIssueDate())
                .expiryDate(fc.getExpiryDate())
                .status(fc.getStatus() != null ? DocStatusType.valueOf(fc.getStatus().name()) : null)
                .documentUrl(fc.getDocumentUrl())
                .notes(fc.getNotes())
                .build();
    }
}
