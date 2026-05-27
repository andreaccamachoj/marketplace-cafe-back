package co.com.marketplace.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.reactive.TransactionCallback;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Configuration
public class TestTxConfig {

    @org.springframework.context.annotation.Primary
    @Bean
    public TransactionalOperator transactionalOperator() {
        System.out.println(">>> TestTxConfig: creating PassThroughTx bean");
        return new PassThroughTx();
    }

    static final class PassThroughTx implements TransactionalOperator {

        @Override
        public <T> Flux<T> execute(TransactionCallback<T> action) {
            System.out.println(">>> PassThroughTx.execute() called!!!");
            return Flux.empty();
        }

        @Override
        public <T> Mono<T> transactional(Mono<T> mono) {
            System.out.println(">>> PassThroughTx.transactional(Mono) called, mono=" + mono);
            return mono;
        }

        @Override
        public <T> Flux<T> transactional(Flux<T> flux) {
            return flux;
        }
    }
}
