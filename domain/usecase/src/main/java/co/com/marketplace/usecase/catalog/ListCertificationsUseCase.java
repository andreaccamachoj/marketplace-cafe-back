package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Certification;
import co.com.marketplace.model.catalog.gateways.CertificationGateway;
import reactor.core.publisher.Flux;

public final class ListCertificationsUseCase {

    private final CertificationGateway certificationGateway;

    public ListCertificationsUseCase(CertificationGateway certificationGateway) {
        this.certificationGateway = certificationGateway;
    }

    public Flux<Certification> execute() {
        return certificationGateway.findAll();
    }
}
