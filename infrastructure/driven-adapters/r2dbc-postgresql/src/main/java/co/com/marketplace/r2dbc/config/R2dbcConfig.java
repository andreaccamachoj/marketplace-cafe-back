package co.com.marketplace.r2dbc.config;

import co.com.marketplace.r2dbc.type.CouponDiscountType;
import co.com.marketplace.r2dbc.type.DocStatusType;
import co.com.marketplace.r2dbc.type.OrderStatusType;
import co.com.marketplace.r2dbc.type.PaymentStatusType;
import co.com.marketplace.r2dbc.type.ProducerStatusType;
import co.com.marketplace.r2dbc.type.ReviewStatusType;
import co.com.marketplace.r2dbc.type.UserStatusType;
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.postgresql.extension.Extension;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Parameter;
import io.r2dbc.spi.Parameters;
import org.springframework.boot.r2dbc.autoconfigure.ConnectionFactoryOptionsBuilderCustomizer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.DialectResolver;
import org.springframework.data.r2dbc.dialect.R2dbcDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "co.com.marketplace.r2dbc")
@EnableR2dbcAuditing
public class R2dbcConfig {

    @Bean
    public ConnectionFactoryOptionsBuilderCustomizer enumCodecCustomizer() {
        Extension enumCodec = EnumCodec.builder()
                .withEnum("user_status",          UserStatusType.class)
                .withEnum("producer_status",      ProducerStatusType.class)
                .withEnum("order_status",         OrderStatusType.class)
                .withEnum("payment_status",       PaymentStatusType.class)
                .withEnum("review_status",        ReviewStatusType.class)
                .withEnum("coupon_discount_type", CouponDiscountType.class)
                .withEnum("doc_status",           DocStatusType.class)
                .build();
        return builder -> builder.option(PostgresqlConnectionFactoryProvider.EXTENSIONS, List.of(enumCodec));
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(ConnectionFactory connectionFactory) {
        R2dbcDialect dialect = DialectResolver.getDialect(connectionFactory);
        return R2dbcCustomConversions.of(dialect, List.of(
                new UserStatusWriter(),         new UserStatusReader(),
                new ProducerStatusWriter(),     new ProducerStatusReader(),
                new OrderStatusWriter(),        new OrderStatusReader(),
                new PaymentStatusWriter(),      new PaymentStatusReader(),
                new ReviewStatusWriter(),       new ReviewStatusReader(),
                new CouponDiscountTypeWriter(), new CouponDiscountTypeReader(),
                new DocStatusWriter(),          new DocStatusReader()
        ));
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager txManager) {
        return TransactionalOperator.create(txManager);
    }

    // ── Writing converters ──────────────────────────────────────────────────

    @WritingConverter
    static class UserStatusWriter implements Converter<UserStatusType, Parameter> {
        @Override public Parameter convert(UserStatusType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class ProducerStatusWriter implements Converter<ProducerStatusType, Parameter> {
        @Override public Parameter convert(ProducerStatusType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class OrderStatusWriter implements Converter<OrderStatusType, Parameter> {
        @Override public Parameter convert(OrderStatusType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class PaymentStatusWriter implements Converter<PaymentStatusType, Parameter> {
        @Override public Parameter convert(PaymentStatusType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class ReviewStatusWriter implements Converter<ReviewStatusType, Parameter> {
        @Override public Parameter convert(ReviewStatusType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class CouponDiscountTypeWriter implements Converter<CouponDiscountType, Parameter> {
        @Override public Parameter convert(CouponDiscountType s) { return Parameters.in(s); }
    }

    @WritingConverter
    static class DocStatusWriter implements Converter<DocStatusType, Parameter> {
        @Override public Parameter convert(DocStatusType s) { return Parameters.in(s); }
    }

    // ── Reading converters ──────────────────────────────────────────────────

    @ReadingConverter
    static class UserStatusReader implements Converter<String, UserStatusType> {
        @Override public UserStatusType convert(String s) { return UserStatusType.valueOf(s); }
    }

    @ReadingConverter
    static class ProducerStatusReader implements Converter<String, ProducerStatusType> {
        @Override public ProducerStatusType convert(String s) { return ProducerStatusType.valueOf(s); }
    }

    @ReadingConverter
    static class OrderStatusReader implements Converter<String, OrderStatusType> {
        @Override public OrderStatusType convert(String s) { return OrderStatusType.valueOf(s); }
    }

    @ReadingConverter
    static class PaymentStatusReader implements Converter<String, PaymentStatusType> {
        @Override public PaymentStatusType convert(String s) { return PaymentStatusType.valueOf(s); }
    }

    @ReadingConverter
    static class ReviewStatusReader implements Converter<String, ReviewStatusType> {
        @Override public ReviewStatusType convert(String s) { return ReviewStatusType.valueOf(s); }
    }

    @ReadingConverter
    static class CouponDiscountTypeReader implements Converter<String, CouponDiscountType> {
        @Override public CouponDiscountType convert(String s) { return CouponDiscountType.valueOf(s); }
    }

    @ReadingConverter
    static class DocStatusReader implements Converter<String, DocStatusType> {
        @Override public DocStatusType convert(String s) { return DocStatusType.valueOf(s); }
    }
}
