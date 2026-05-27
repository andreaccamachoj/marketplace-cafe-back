package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.UserGateway;
import reactor.core.publisher.Flux;

public final class ListUsersUseCase {

    private final UserGateway userGateway;

    public ListUsersUseCase(UserGateway userGateway) {
        this.userGateway = userGateway;
    }

    public Flux<User> execute(String roleFilter, UserStatus statusFilter, String search, int page, int size) {
        return userGateway.findAll(roleFilter, statusFilter, search, page, size);
    }
}
