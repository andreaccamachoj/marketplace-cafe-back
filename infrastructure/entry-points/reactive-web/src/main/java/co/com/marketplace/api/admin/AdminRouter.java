package co.com.marketplace.api.admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AdminRouter {

    @Bean
    public RouterFunction<ServerResponse> adminRoutes(AdminHandler h) {
        return route()
                .GET("/api/admin/users",                                    h::listUsers)
                .PATCH("/api/admin/users/{id}/ban",                         accept(MediaType.APPLICATION_JSON), h::banUser)
                .PATCH("/api/admin/users/{id}/unban",                       h::unbanUser)
                .GET("/api/admin/producer-approvals",                       h::listApprovals)
                .PATCH("/api/admin/producer-approvals/{id}/approve",        accept(MediaType.APPLICATION_JSON), h::approveProducer)
                .PATCH("/api/admin/producer-approvals/{id}/reject",         accept(MediaType.APPLICATION_JSON), h::rejectProducer)
                .GET("/api/admin/categories",                               h::listAdminCategories)
                .POST("/api/admin/categories",                              accept(MediaType.APPLICATION_JSON), h::createCategory)
                .GET("/api/admin/products",                                 h::listAdminProducts)
                .PATCH("/api/admin/products/{id}/activate",                 h::activateProduct)
                .PUT("/api/admin/categories/{id}",                          accept(MediaType.APPLICATION_JSON), h::updateCategory)
                .DELETE("/api/admin/categories/{id}",                       h::deleteCategory)
                .GET("/api/admin/activity",                                 h::listActivity)
                .build();
    }
}
