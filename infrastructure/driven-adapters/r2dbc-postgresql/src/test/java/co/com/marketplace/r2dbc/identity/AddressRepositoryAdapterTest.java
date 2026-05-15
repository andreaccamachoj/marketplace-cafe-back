package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressRepositoryAdapterTest {

    @Mock private AddressReactiveRepository repository;
    @Mock private R2dbcEntityTemplate template;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;

    @InjectMocks private AddressRepositoryAdapter adapter;

    private final UUID addressId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private AddressData addressData;
    private Address address;

    @BeforeEach
    void setUp() {
        addressData = AddressData.builder()
                .id(addressId)
                .userId(userId)
                .label("Casa")
                .line1("Calle 1")
                .line2("Apto 1")
                .city("Bogotá")
                .department("Cundinamarca")
                .zipCode("110111")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        address = Address.builder()
                .id(addressId)
                .userId(userId)
                .label("Casa")
                .line1("Calle 1")
                .line2("Apto 1")
                .city("Bogotá")
                .department("Cundinamarca")
                .zipCode("110111")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsAddress_whenSuccessful() {
        when(repository.save(any(AddressData.class))).thenReturn(Mono.just(addressData));

        StepVerifier.create(adapter.save(address))
                .expectNextMatches(a -> addressId.equals(a.getId()) && "Casa".equals(a.getLabel()))
                .verifyComplete();
    }

    @Test
    void findById_returnsAddress_whenFound() {
        when(repository.findById(addressId)).thenReturn(Mono.just(addressData));

        StepVerifier.create(adapter.findById(addressId))
                .expectNextMatches(a -> addressId.equals(a.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repository.findById(addressId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(addressId))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsList_whenFound() {
        when(repository.findByUserId(userId)).thenReturn(Flux.just(addressData));

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNextMatches(a -> userId.equals(a.getUserId()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNone() {
        when(repository.findByUserId(userId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyComplete();
    }

    @Test
    void update_returnsUpdatedAddress_whenSuccessful() {
        when(template.update(any(AddressData.class))).thenReturn(Mono.just(addressData));

        StepVerifier.create(adapter.update(address))
                .expectNextMatches(a -> addressId.equals(a.getId()))
                .verifyComplete();
    }

    @Test
    void deleteById_completesSuccessfully() {
        when(repository.deleteById(addressId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteById(addressId))
                .verifyComplete();
    }

    @Test
    void clearDefaultForUser_completesSuccessfully() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.clearDefaultForUser(userId))
                .verifyComplete();
    }

    @Test
    void clearDefaultForUser_propagatesError() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.clearDefaultForUser(userId))
                .verifyError(RuntimeException.class);
    }
}
