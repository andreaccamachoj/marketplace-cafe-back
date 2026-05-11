package co.com.marketplace.r2dbc.payments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "payment_methods")
public class PaymentMethodData {
    @Id
    private UUID id;
    private String code;
    private String name;
    private String type;
    @Column("account_number")
    private String accountNumber;
    @Column("account_holder")
    private String accountHolder;
    private String bank;
    private String alias;
    private String nit;
    private String emoji;
    @Column("accent_color")
    private String accentColor;
    @Column("is_active")
    private boolean isActive;
    @Column("display_order")
    private int displayOrder;
}
