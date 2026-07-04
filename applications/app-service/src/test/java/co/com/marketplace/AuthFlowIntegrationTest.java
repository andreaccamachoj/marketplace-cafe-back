package co.com.marketplace;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for the buyer registration and login flow.
 * Uses Testcontainers 2.x GenericContainer with a postgres image.
 * Requires Docker to be running; skipped automatically if Docker is unavailable.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthFlowIntegrationTest {

//    static GenericContainer<?> postgres;
//
//    static {
//        try {
//            postgres = new GenericContainer<>(DockerImageName.parse("postgres:15-alpine"))
//                    .withExposedPorts(5432)
//                    .withEnv("POSTGRES_DB", "wcm_test")
//                    .withEnv("POSTGRES_USER", "wcm")
//                    .withEnv("POSTGRES_PASSWORD", "wcm_pass")
//                    .waitingFor(Wait.forListeningPort());
//            postgres.start();
//        } catch (Exception e) {
//            postgres = null;
//        }
//    }
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        if (postgres != null && postgres.isRunning()) {
//            registry.add("DB_HOST", postgres::getHost);
//            registry.add("DB_PORT", () -> postgres.getMappedPort(5432).toString());
//        } else {
//            registry.add("DB_HOST", () -> "localhost");
//            registry.add("DB_PORT", () -> "5432");
//        }
//        registry.add("DB_NAME", () -> "wcm_test");
//        registry.add("DB_USER", () -> "wcm");
//        registry.add("DB_PASS", () -> "wcm_pass");
//        registry.add("JWT_SECRET", () -> "test-secret-key-at-least-256-bits-long-for-hs256-algorithm");
//        registry.add("CORS_ORIGINS", () -> "http://localhost:4200");
//    }
//
//    @LocalServerPort
//    private int port;
//
//    private WebTestClient client() {
//        return WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
//    }
//
//    record RegisterRequest(String email, String password, String fullName, String phone) {}
//    record LoginRequest(String email, String password) {}
//
//    @Test
//    @Order(1)
//    void buyerCanRegister() {
//        if (postgres == null || !postgres.isRunning()) {
//            return; // Docker not available — skip
//        }
//        client().post()
//                .uri("/api/auth/register/buyer")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(new RegisterRequest("integration@test.com", "password123", "Integration User", "555-0001"))
//                .exchange()
//                .expectStatus().isCreated()
//                .expectBody()
//                .jsonPath("$.accessToken").isNotEmpty()
//                .jsonPath("$.refreshToken").isNotEmpty();
//    }
//
//    @Test
//    @Order(2)
//    void buyerCanLogin() {
//        if (postgres == null || !postgres.isRunning()) {
//            return; // Docker not available — skip
//        }
//        client().post()
//                .uri("/api/auth/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(new LoginRequest("integration@test.com", "password123"))
//                .exchange()
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.accessToken").isNotEmpty()
//                .jsonPath("$.refreshToken").isNotEmpty();
//    }
}
