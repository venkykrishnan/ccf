package ccf.domain.user;

import akka.javasdk.annotations.TypeName;

import java.time.Instant;

public sealed interface UserEvent {
    @TypeName("user-created")
    record UserCreated(User.UserCreateInfo createInfo) implements UserEvent {
    }
    @TypeName("user-status-changed")
    record UserStatusChanged(Boolean enable) implements UserEvent {
    }
}
