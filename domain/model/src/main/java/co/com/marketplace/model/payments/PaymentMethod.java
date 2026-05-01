package co.com.marketplace.model.payments;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class PaymentMethod {
    UUID id;
    String code;
    String name;
    String type;
    String accountNumber;
    String accountHolder;
    String bank;
    String alias;
    String nit;
    String emoji;
    String accentColor;
    boolean isActive;
    int displayOrder;
}
