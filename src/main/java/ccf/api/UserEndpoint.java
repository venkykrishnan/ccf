package ccf.api;

import akka.http.javadsl.model.HttpResponse;
import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.annotations.http.Post;
import akka.javasdk.annotations.http.Put;
import akka.javasdk.client.ComponentClient;
import akka.javasdk.http.HttpResponses;

import ccf.domain.user.User;
import ccf.application.UserEntity;
import ccf.application.UsersByFilterView;
import ccf.domain.user.Users;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletionStage;

@Acl(allow = @Acl.Matcher(principal = Acl.Principal.INTERNET))
@HttpEndpoint("/users")
public class UserEndpoint {
    private final ComponentClient componentClient;

    private static final Logger logger = LoggerFactory.getLogger(UserEndpoint.class);

    public UserEndpoint(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @Get("/{userId}")
    public CompletionStage<User> get(String userId) {
        CCFLog.debug(logger, "Getting user",
                Map.of("userId", userId));
        return componentClient.forEventSourcedEntity(userId)
                .method(UserEntity::getUser)
                .invokeAsync();
    }

    @Post("/{userId}/create")
    public CompletionStage<HttpResponse> createUser(String userId,
                                                       User.UserInput userInput
    ) {
        CCFLog.debug(logger, "creating user",
                Map.of("userId", userId, "userInput", userInput.toString()));
        return componentClient.forEventSourcedEntity(userId)
                .method(UserEntity::createUser)
                .invokeAsync(userInput)
                .thenApply(__ -> HttpResponses.ok());
    }

    @Put("/{userId}/statusChange")
    public CompletionStage<HttpResponse> statusChange(String userId,
                                                    boolean enable
    ) {
        CCFLog.debug(logger, "creating user",
                Map.of("userId", userId, "toEnable", String.valueOf(enable)));
        return componentClient.forEventSourcedEntity(userId)
                .method(UserEntity::changeStatus)
                .invokeAsync(enable)
                .thenApply(__ -> HttpResponses.ok());
    }

    @Get("/all")
    public CompletionStage<Users> usersAll() {
        CCFLog.debug(logger, "get users", Map.of());
        return componentClient.forView()
                .method(UsersByFilterView::getAllUsers)
                .invokeAsync();
    }
}
