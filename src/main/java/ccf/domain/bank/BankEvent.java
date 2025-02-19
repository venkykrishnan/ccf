package ccf.domain.bank;

import akka.javasdk.annotations.TypeName;

public sealed interface BankEvent {
    @TypeName("bank-created")
    record BankCreated(Bank.BankCreateInfo createInfo) implements BankEvent {
    }
    @TypeName("bank-user-added")
    record BankUserAdded(String userId) implements BankEvent {
    }
}
