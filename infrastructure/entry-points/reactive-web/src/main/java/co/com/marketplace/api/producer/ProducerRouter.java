package co.com.marketplace.api.producer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ProducerRouter {

    @Bean
    public RouterFunction<ServerResponse> producerRoutes(ProducerHandler h) {
        return route()
                .GET("/api/producer/products",                              h::listMyProducts)
                .POST("/api/producer/products",                             accept(MediaType.APPLICATION_JSON), h::createProduct)
                .PUT("/api/producer/products/{id}",                         accept(MediaType.APPLICATION_JSON), h::updateProduct)
                .POST("/api/producer/products/{id}/archive",                h::archiveProduct)
                .GET("/api/producer/orders",                                h::listOrders)
                .PATCH("/api/producer/orders/{id}/status",                  accept(MediaType.APPLICATION_JSON), h::updateOrderStatus)
                .POST("/api/producer/orders/{id}/payment/confirm",          accept(MediaType.APPLICATION_JSON), h::confirmPayment)
                .GET("/api/producer/farm",                                  h::getFarm)
                .PATCH("/api/producer/farm",                                accept(MediaType.APPLICATION_JSON), h::updateFarm)
                .GET("/api/producer/farm/certifications",                   h::getFarmCertifications)
                .POST("/api/producer/farm/certifications",                  accept(MediaType.APPLICATION_JSON), h::addFarmCertification)
                .DELETE("/api/producer/farm/certifications/{id}",           h::removeFarmCertification)
                .GET("/api/producer/reviews",                               h::listReviews)
                .GET("/api/producer/inventory",                             h::getInventory)
                .POST("/api/producer/inventory/adjust",                     accept(MediaType.APPLICATION_JSON), h::adjustInventory)
                .build();
    }
}
