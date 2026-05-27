package co.com.marketplace.model.catalog;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ProductCupping {
    UUID productId;
    BigDecimal score;
    Short aroma;
    Short flavor;
    Short body;
    Short finish;
    Short acidity;
}
