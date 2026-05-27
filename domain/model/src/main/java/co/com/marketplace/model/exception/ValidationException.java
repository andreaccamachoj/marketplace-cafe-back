package co.com.marketplace.model.exception;

public class ValidationException extends DomainException {
    public ValidationException(String code, String message) {
        super(code, message);
    }
}
