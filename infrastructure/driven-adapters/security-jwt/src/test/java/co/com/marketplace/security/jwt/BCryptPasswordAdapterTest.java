package co.com.marketplace.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class BCryptPasswordAdapterTest {

    private BCryptPasswordAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new BCryptPasswordAdapter(new BCryptPasswordEncoder());
    }

    // --- encode ---

    @Test
    void encode_resultNotNullOrEmpty() {
        String result = adapter.encode("mySecret123");
        assertNotNull(result);
        assertFalse(result.isBlank());
    }

    @Test
    void encode_resultDiffersFromRaw() {
        String raw = "mySecret123";
        assertNotEquals(raw, adapter.encode(raw));
    }

    @Test
    void encode_sameInputProducesDifferentHashes() {
        String raw = "mySecret123";
        assertNotEquals(adapter.encode(raw), adapter.encode(raw));
    }

    // --- matches ---

    @Test
    void matches_correctPassword_returnsTrue() {
        String raw = "mySecret123";
        String encoded = adapter.encode(raw);
        assertTrue(adapter.matches(raw, encoded));
    }

    @Test
    void matches_wrongPassword_returnsFalse() {
        String encoded = adapter.encode("correctPassword");
        assertFalse(adapter.matches("wrongPassword", encoded));
    }

    @Test
    void matches_emptyAgainstValidHash_returnsFalse() {
        String encoded = adapter.encode("somePassword");
        assertFalse(adapter.matches("", encoded));
    }
}
