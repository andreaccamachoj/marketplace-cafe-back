package co.com.marketplace.api.catalog;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class CatalogRouter {

    @Bean
    public RouterFunction<ServerResponse> catalogRoutes(CatalogHandler h) {
        return route()
                .GET("/api/catalog/products",                    h::listProducts)
                .GET("/api/catalog/products/featured",           h::getFeaturedProducts)
                .GET("/api/catalog/products/{id}",               h::getProductById)
                .GET("/api/catalog/products/by-slug/{slug}",     h::getProductBySlug)
                .GET("/api/catalog/products/{id}/reviews",       h::getProductReviews)
                .GET("/api/catalog/categories",                  h::listCategories)
                .GET("/api/catalog/categories/{slug}",           h::getCategoryBySlug)
                .GET("/api/catalog/certifications",              h::listCertifications)
                .GET("/api/catalog/roast-levels",                h::listRoastLevels)
                .build();
    }
}
