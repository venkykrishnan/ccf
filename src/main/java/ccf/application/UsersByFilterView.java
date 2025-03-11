package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.user.UserEvent;
import ccf.domain.user.UserRow;
import ccf.domain.user.Users;


@ComponentId("users_by_filter")
public class UsersByFilterView extends View {

    @Consume.FromEventSourcedEntity(UserEntity.class)
    public static class UsersByFilter extends TableUpdater<UserRow> { // <2>
        public Effect<UserRow> onEvent(UserEvent event) { // <3>
            var ret = switch (event) {
                case UserEvent.UserCreated created->
                        // User.UserCreateInfo
                        effects().updateRow(new UserRow(created.createInfo().userId(),
                                created.createInfo().fullName(),
                                null, created.createInfo().status()));
//                    effects().updateRow(new UserRow(created.createInfo().userId(),
//                            created.createInfo().fullName(),
//                            created.createInfo().role(), created.createInfo().status()));
                case UserEvent.UserStatusChanged statusChanged ->
                        effects().updateRow(rowState().onStatusChanged(statusChanged.enable()));
            };
            return ret;
        }
    }

    @Query("SELECT * AS users FROM users_by_filter")
    public QueryEffect<Users> getAllUsers() {
        return queryResult();
    }
}
