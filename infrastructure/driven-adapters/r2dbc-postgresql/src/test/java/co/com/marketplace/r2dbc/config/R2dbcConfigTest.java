package co.com.marketplace.r2dbc.config;

import co.com.marketplace.r2dbc.type.CouponDiscountType;
import co.com.marketplace.r2dbc.type.DocStatusType;
import co.com.marketplace.r2dbc.type.OrderStatusType;
import co.com.marketplace.r2dbc.type.PaymentStatusType;
import co.com.marketplace.r2dbc.type.ProducerStatusType;
import co.com.marketplace.r2dbc.type.ReviewStatusType;
import co.com.marketplace.r2dbc.type.UserStatusType;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Parameter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.r2dbc.autoconfigure.ConnectionFactoryOptionsBuilderCustomizer;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class R2dbcConfigTest {

    private final R2dbcConfig config = new R2dbcConfig();

    @Test
    void enumCodecCustomizer_returnsNonNull() {
        ConnectionFactoryOptionsBuilderCustomizer customizer = config.enumCodecCustomizer();
        assertThat(customizer).isNotNull();
    }

    @Test
    void transactionManager_returnsNonNull() {
        ConnectionFactory cf = mock(ConnectionFactory.class);
        ReactiveTransactionManager txManager = config.transactionManager(cf);
        assertThat(txManager).isNotNull();
    }

    @Test
    void transactionalOperator_returnsNonNull() {
        ReactiveTransactionManager txManager = mock(ReactiveTransactionManager.class);
        TransactionalOperator op = config.transactionalOperator(txManager);
        assertThat(op).isNotNull();
    }

    // ── UserStatus ────────────────────────────────────────────────────────────

    @Test
    void userStatusWriter_active_returnsParameter() {
        R2dbcConfig.UserStatusWriter writer = new R2dbcConfig.UserStatusWriter();
        Parameter p = writer.convert(UserStatusType.active);
        assertThat(p).isNotNull();
    }

    @Test
    void userStatusWriter_inactive_returnsParameter() {
        R2dbcConfig.UserStatusWriter writer = new R2dbcConfig.UserStatusWriter();
        assertThat(writer.convert(UserStatusType.inactive)).isNotNull();
    }

    @Test
    void userStatusWriter_banned_returnsParameter() {
        R2dbcConfig.UserStatusWriter writer = new R2dbcConfig.UserStatusWriter();
        assertThat(writer.convert(UserStatusType.banned)).isNotNull();
    }

    @Test
    void userStatusReader_active_convertsCorrectly() {
        R2dbcConfig.UserStatusReader reader = new R2dbcConfig.UserStatusReader();
        assertThat(reader.convert("active")).isEqualTo(UserStatusType.active);
    }

    @Test
    void userStatusReader_inactive_convertsCorrectly() {
        R2dbcConfig.UserStatusReader reader = new R2dbcConfig.UserStatusReader();
        assertThat(reader.convert("inactive")).isEqualTo(UserStatusType.inactive);
    }

    @Test
    void userStatusReader_banned_convertsCorrectly() {
        R2dbcConfig.UserStatusReader reader = new R2dbcConfig.UserStatusReader();
        assertThat(reader.convert("banned")).isEqualTo(UserStatusType.banned);
    }

    // ── ProducerStatus ────────────────────────────────────────────────────────

    @Test
    void producerStatusWriter_returnsParameter() {
        R2dbcConfig.ProducerStatusWriter writer = new R2dbcConfig.ProducerStatusWriter();
        assertThat(writer.convert(ProducerStatusType.pending)).isNotNull();
        assertThat(writer.convert(ProducerStatusType.approved)).isNotNull();
        assertThat(writer.convert(ProducerStatusType.rejected)).isNotNull();
    }

    @Test
    void producerStatusReader_convertsAllValues() {
        R2dbcConfig.ProducerStatusReader reader = new R2dbcConfig.ProducerStatusReader();
        assertThat(reader.convert("pending")).isEqualTo(ProducerStatusType.pending);
        assertThat(reader.convert("approved")).isEqualTo(ProducerStatusType.approved);
        assertThat(reader.convert("rejected")).isEqualTo(ProducerStatusType.rejected);
    }

    // ── OrderStatus ───────────────────────────────────────────────────────────

    @Test
    void orderStatusWriter_returnsParameter() {
        R2dbcConfig.OrderStatusWriter writer = new R2dbcConfig.OrderStatusWriter();
        assertThat(writer.convert(OrderStatusType.pending_verification)).isNotNull();
        assertThat(writer.convert(OrderStatusType.confirmed)).isNotNull();
        assertThat(writer.convert(OrderStatusType.cancelled)).isNotNull();
    }

    @Test
    void orderStatusReader_convertsValues() {
        R2dbcConfig.OrderStatusReader reader = new R2dbcConfig.OrderStatusReader();
        assertThat(reader.convert("pending_verification")).isEqualTo(OrderStatusType.pending_verification);
        assertThat(reader.convert("confirmed")).isEqualTo(OrderStatusType.confirmed);
        assertThat(reader.convert("delivered")).isEqualTo(OrderStatusType.delivered);
        assertThat(reader.convert("cancelled")).isEqualTo(OrderStatusType.cancelled);
    }

    // ── PaymentStatus ─────────────────────────────────────────────────────────

    @Test
    void paymentStatusWriter_returnsParameter() {
        R2dbcConfig.PaymentStatusWriter writer = new R2dbcConfig.PaymentStatusWriter();
        assertThat(writer.convert(PaymentStatusType.submitted)).isNotNull();
        assertThat(writer.convert(PaymentStatusType.verified)).isNotNull();
        assertThat(writer.convert(PaymentStatusType.rejected)).isNotNull();
        assertThat(writer.convert(PaymentStatusType.refunded)).isNotNull();
    }

    @Test
    void paymentStatusReader_convertsAllValues() {
        R2dbcConfig.PaymentStatusReader reader = new R2dbcConfig.PaymentStatusReader();
        assertThat(reader.convert("submitted")).isEqualTo(PaymentStatusType.submitted);
        assertThat(reader.convert("verified")).isEqualTo(PaymentStatusType.verified);
        assertThat(reader.convert("rejected")).isEqualTo(PaymentStatusType.rejected);
        assertThat(reader.convert("refunded")).isEqualTo(PaymentStatusType.refunded);
    }

    // ── ReviewStatus ──────────────────────────────────────────────────────────

    @Test
    void reviewStatusWriter_returnsParameter() {
        R2dbcConfig.ReviewStatusWriter writer = new R2dbcConfig.ReviewStatusWriter();
        assertThat(writer.convert(ReviewStatusType.published)).isNotNull();
        assertThat(writer.convert(ReviewStatusType.hidden)).isNotNull();
        assertThat(writer.convert(ReviewStatusType.reported)).isNotNull();
    }

    @Test
    void reviewStatusReader_convertsAllValues() {
        R2dbcConfig.ReviewStatusReader reader = new R2dbcConfig.ReviewStatusReader();
        assertThat(reader.convert("published")).isEqualTo(ReviewStatusType.published);
        assertThat(reader.convert("hidden")).isEqualTo(ReviewStatusType.hidden);
        assertThat(reader.convert("reported")).isEqualTo(ReviewStatusType.reported);
    }

    // ── CouponDiscountType ────────────────────────────────────────────────────

    @Test
    void couponDiscountTypeWriter_returnsParameter() {
        R2dbcConfig.CouponDiscountTypeWriter writer = new R2dbcConfig.CouponDiscountTypeWriter();
        assertThat(writer.convert(CouponDiscountType.percentage)).isNotNull();
        assertThat(writer.convert(CouponDiscountType.fixed)).isNotNull();
    }

    @Test
    void couponDiscountTypeReader_convertsAllValues() {
        R2dbcConfig.CouponDiscountTypeReader reader = new R2dbcConfig.CouponDiscountTypeReader();
        assertThat(reader.convert("percentage")).isEqualTo(CouponDiscountType.percentage);
        assertThat(reader.convert("fixed")).isEqualTo(CouponDiscountType.fixed);
    }

    // ── DocStatus ─────────────────────────────────────────────────────────────

    @Test
    void docStatusWriter_returnsParameter() {
        R2dbcConfig.DocStatusWriter writer = new R2dbcConfig.DocStatusWriter();
        assertThat(writer.convert(DocStatusType.pending)).isNotNull();
        assertThat(writer.convert(DocStatusType.approved)).isNotNull();
        assertThat(writer.convert(DocStatusType.rejected)).isNotNull();
    }

    @Test
    void docStatusReader_convertsAllValues() {
        R2dbcConfig.DocStatusReader reader = new R2dbcConfig.DocStatusReader();
        assertThat(reader.convert("pending")).isEqualTo(DocStatusType.pending);
        assertThat(reader.convert("approved")).isEqualTo(DocStatusType.approved);
        assertThat(reader.convert("rejected")).isEqualTo(DocStatusType.rejected);
    }
}
