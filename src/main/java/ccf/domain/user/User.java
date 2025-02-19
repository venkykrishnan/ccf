package ccf.domain.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record User(String userId,
                   String fullName,
                   UserRole role,
                   UserInstanceType instanceType,
                   UserStatus status,
                   Instant creationTimestamp, Instant modificationTimestamp
                      ) {
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public record UserInput(String fullName, UserRole role, UserInstanceType instanceType) {
    }

    // First time user record. This is also used in views
    public record UserCreateInfo(String userId,
                                    String fullName,
                                    UserRole role,
                                    UserInstanceType instanceType,
                                    UserStatus status,
                                    Instant creationTimestamp, Instant modificationTimestamp) {
    }

    public User onUserCreated(UserEvent.UserCreated userCreated) {
        UserCreateInfo createInfo = userCreated.createInfo();
        return new User(createInfo.userId, createInfo.fullName, createInfo.role,
                createInfo.instanceType,
                createInfo.status,
                createInfo.creationTimestamp, createInfo.modificationTimestamp);
    }
    public User onUserStatusChanged(UserEvent.UserStatusChanged userStatusChanged) {
        UserStatus newStatus = (userStatusChanged.enable() ? UserStatus.BANK_ENABLED : UserStatus.USER_DISABLED);
        return new User(userId, fullName, role, instanceType,
                newStatus, creationTimestamp, modificationTimestamp);
    }
}
