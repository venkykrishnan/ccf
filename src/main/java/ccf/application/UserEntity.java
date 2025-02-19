package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.user.*;
import ccf.util.CCFLog;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

@ComponentId("user")
public class UserEntity extends EventSourcedEntity<User, UserEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(UserEntity.class);

    // constructor used to initialize the entityId
    public UserEntity(EventSourcedEntityContext context) {
        entityId = context.entityId();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = UserResult.Success.class, name = "Success"),
            @JsonSubTypes.Type(value = UserResult.Failure.class, name = "Failure")})
    public sealed interface UserResult {

        record Failure(String message) implements UserResult {
        }

        record Success() implements UserResult {
        }
    }

    @Override
    public User emptyState() {
        return new User(entityId, null, UserRole.USER_ROLE_INTERNAL, UserInstanceType.USER_INSTANCE_TYPE_ACTUAL,
                UserStatus.USER_NOT_INITIALIZED, Instant.now(), Instant.now());
    }

    public ReadOnlyEffect<User> getUser() {
        return effects().reply(currentState());
    }

    public Effect<Done> createUser(User.UserInput userInput) {
        if (currentState().status() != UserStatus.USER_NOT_INITIALIZED) {
            logger.info("User id={} is already created", entityId);
            return effects().error("User is already created");
        }

        try {
//            CCFLogger.log(logger,"Create company", Map.of("company_id", entityId, "metadata", companyMetadata.toString()));
//            MDC.put("company_id", entityId);
//            MDC.put("metadata", companyMetadata.toString());
//            logger.info("Creating company");
//            MDC.clear();
            User.UserCreateInfo userCreateInfo =
                    new User.UserCreateInfo(entityId, userInput.fullName(), userInput.role(), userInput.instanceType(),
                            UserStatus.BANK_ENABLED, Instant.now(), Instant.now());
            var event = new UserEvent.UserCreated(userCreateInfo);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            logger.info("Creating user failed:{} ",e.getMessage());
            return effects().error("create user failed");
        }
    }
    public Effect<UserResult> changeStatus(boolean enable) {
        logger.info("Changing user status userId={} To be enableds= {}", entityId, enable);
        if (currentState().status() == UserStatus.USER_NOT_INITIALIZED) {
            CCFLog.error(logger, "Change user status",
                    Map.of("userId", entityId));
            return effects().reply(new UserResult.Failure("User is not an initialized state"));
        }

        var event = new UserEvent.UserStatusChanged(enable);

        return effects()
                .persist(event)
                .thenReply(newState -> new UserResult.Success());
    }

    @Override
    public User applyEvent(UserEvent event) {
        return switch (event) {
            case UserEvent.UserCreated evt -> currentState().onUserCreated(evt);
            case UserEvent.UserStatusChanged evt -> currentState().onUserStatusChanged(evt);
        };
    }

}
