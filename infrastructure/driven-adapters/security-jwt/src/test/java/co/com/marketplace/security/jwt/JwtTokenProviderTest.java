package co.com.marketplace.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm";
    private static final long ACCESS_TTL = 3600L;
    private static final long REFRESH_TTL = 86400L;

    private JwtTokenProvider provider;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(SECRET, ACCESS_TTL, REFRESH_TTL);
        secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // --- isTokenValid / extractEmail / extractRole ---

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = provider.generateAccessToken(UUID.randomUUID(), "a@b.com", "BUYER");
        assertTrue(provider.isTokenValid(token));
    }

    @Test
    void isTokenValid_malformedToken_returnsFalse() {
        assertFalse(provider.isTokenValid("bad.token.value"));
    }

    @Test
    void isTokenValid_wrongSignature_returnsFalse() {
        JwtTokenProvider other = new JwtTokenProvider(
                "another-secret-key-that-is-long-enough-for-hmac-sha256", ACCESS_TTL, REFRESH_TTL);
        String token = other.generateAccessToken(UUID.randomUUID(), "a@b.com", "BUYER");
        assertFalse(provider.isTokenValid(token));
    }

    @Test
    void extractEmail_validToken_returnsEmail() {
        String token = provider.generateAccessToken(UUID.randomUUID(), "cafe@test.com", "BUYER");
        assertEquals("cafe@test.com", provider.extractEmail(token));
    }

    @Test
    void extractRole_validToken_returnsRole() {
        String token = provider.generateAccessToken(UUID.randomUUID(), "a@b.com", "PRODUCER");
        assertEquals("PRODUCER", provider.extractRole(token));
    }

    // --- validateToken ---

    @Test
    void validateToken_validToken_emitsCorrectUserId() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "user@test.com", "BUYER");
        StepVerifier.create(provider.validateToken(token))
                .expectNext(userId)
                .verifyComplete();
    }

    @Test
    void validateToken_malformedToken_completesWithError() {
        StepVerifier.create(provider.validateToken("not.a.jwt.token"))
                .verifyError();
    }

    @Test
    void validateToken_invalidSignature_completesWithError() {
        JwtTokenProvider other = new JwtTokenProvider(
                "another-secret-key-that-is-long-enough-for-hmac-sha256", ACCESS_TTL, REFRESH_TTL);
        UUID userId = UUID.randomUUID();
        String tokenFromOther = other.generateAccessToken(userId, "x@x.com", "BUYER");
        StepVerifier.create(provider.validateToken(tokenFromOther))
                .verifyError();
    }

    // --- generateRefreshToken ---

    @Test
    void generateRefreshToken_notNullOrEmpty() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateRefreshToken(userId);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateRefreshToken_subjectIsUserIdAndTypeIsRefresh() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateRefreshToken(userId);
        Claims claims = parse(token);
        assertEquals(userId.toString(), claims.getSubject());
        assertEquals("refresh", claims.get("type", String.class));
    }

    // --- generateAccessToken ---

    @Test
    void generateAccessToken_notNullOrEmpty() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "user@test.com", "BUYER");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateAccessToken_subjectIsUserId() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "user@test.com", "BUYER");
        Claims claims = parse(token);
        assertEquals(userId.toString(), claims.getSubject());
    }

    @Test
    void generateAccessToken_claimsContainEmailAndRole() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateAccessToken(userId, "user@test.com", "PRODUCER");
        Claims claims = parse(token);
        assertEquals("user@test.com", claims.get("email", String.class));
        assertEquals("PRODUCER", claims.get("role", String.class));
    }
}
