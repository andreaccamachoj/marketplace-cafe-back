package co.com.marketplace.model.gateway;

public interface PasswordEncoderGateway {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
