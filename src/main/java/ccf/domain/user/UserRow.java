package ccf.domain.user;


public record UserRow(
        String userId,
        String fullName,
        UserRole role,
        UserStatus status
        ) {
    public UserRow onStatusChanged(Boolean enable) {
        // TODO: need to fix this
        return new UserRow(userId, fullName, role, status);
    }
}
