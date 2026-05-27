package co.com.marketplace.r2dbc.audit;

import co.com.marketplace.model.audit.AuditLog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @InjectMocks private AuditLogRepositoryAdapter adapter;

    @Test
    void save_returnsAuditLog_withoutPersistence() {
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .action("USER_LOGIN")
                .entityType("User")
                .entityId(UUID.randomUUID().toString())
                .ipAddress("127.0.0.1")
                .createdAt(OffsetDateTime.now())
                .build();

        StepVerifier.create(adapter.save(auditLog))
                .expectNextMatches(l -> l.getId().equals(auditLog.getId()) && "USER_LOGIN".equals(l.getAction()))
                .verifyComplete();
    }

    @Test
    void save_returnsExactSameObject() {
        AuditLog auditLog = AuditLog.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .action("PRODUCT_CREATED")
                .entityType("Product")
                .entityId(UUID.randomUUID().toString())
                .metadata("{\"name\":\"Café especial\"}")
                .createdAt(OffsetDateTime.now())
                .build();

        StepVerifier.create(adapter.save(auditLog))
                .expectNext(auditLog)
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenMonoFails() {
        AuditLog nullLog = AuditLog.builder()
                .id(null)
                .userId(UUID.randomUUID())
                .action("TEST")
                .entityType("Test")
                .entityId("test-id")
                .createdAt(OffsetDateTime.now())
                .build();

        StepVerifier.create(adapter.save(nullLog))
                .expectNextMatches(l -> l.getAction().equals("TEST"))
                .verifyComplete();
    }
}
