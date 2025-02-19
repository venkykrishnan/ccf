package ccf.domain.bank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public record BankRow(
        String companyId,
        List<String> users,
        Instant creationTimestamp
        ) {
    public BankRow onBankUserAdded(String user) {
        List<String> localUsers = new ArrayList<String>();
        localUsers.addAll(users);
        localUsers.add(user);
        return new BankRow(companyId, localUsers, creationTimestamp);
    }
}
