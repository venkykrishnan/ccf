package ccf.domain.tabular;

import ccf.domain.bank.BankEvent;
import ccf.domain.bank.BankInstanceType;
import ccf.domain.bank.BankStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// HIA: 10 Mar 25 TODO work on standard dimensions & types
public record Dimension(String name, String description) {
}
public record Bank(String bankId,
                   ccf.domain.bank.Bank.BankMetadata metadata,
                   BankInstanceType instanceType,
                   List<String> users,
                   BankStatus status,
                   Instant creationTimestamp, Instant modificationTimestamp
) {
    private static final Logger logger = LoggerFactory.getLogger(ccf.domain.bank.Bank.class);

    public record NewUser(String userId) {
    }

    // First time bank record. This is also used in views
    public record BankCreateInfo(String bankId,
                                 ccf.domain.bank.Bank.BankMetadata metadata,
                                 BankInstanceType instanceType,
                                 List<String> users,
                                 BankStatus status,
                                 Instant creationTimestamp, Instant modificationTimestamp) {
    }

    public record BankMetadata(Integer naicsCode, URL url) {
    }

    public ccf.domain.bank.Bank onBankCreated(BankEvent.BankCreated bankCreated) {
        ccf.domain.bank.Bank.BankCreateInfo createInfo = bankCreated.createInfo();
        return new ccf.domain.bank.Bank(createInfo.bankId, createInfo.metadata(),
                createInfo.instanceType, createInfo.users,
                createInfo.status,
                createInfo.creationTimestamp, createInfo.modificationTimestamp);
    }

    public ccf.domain.bank.Bank onBankUserAdded(BankEvent.BankUserAdded bankUserAdded) {
        logger.info("Adding user to bank id={} metadata= {}, userId={}", bankId, metadata,bankUserAdded.userId());
        List<String> updatedUsers = Stream.concat(
                Optional.ofNullable(users).stream().flatMap(List::stream),
                Stream.of(bankUserAdded.userId())
        ).toList();

        return new ccf.domain.bank.Bank(bankId, metadata, instanceType, updatedUsers,
                BankStatus.BANK_INITIALIZED, creationTimestamp, Instant.now());
    }
}
