package ccf.domain.bank;

import ccf.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record Bank(String bankId,
                   BankMetadata metadata,
                   BankInstanceType instanceType,
                   List<String> users,
                   BankStatus status,
                   Instant creationTimestamp, Instant modificationTimestamp
                      ) {
    private static final Logger logger = LoggerFactory.getLogger(Bank.class);

    public record NewUser(String userId) {
    }

    // First time bank record. This is also used in views
    public record BankCreateInfo(String bankId,
                                    BankMetadata metadata,
                                    BankInstanceType instanceType,
                                    List<String> users,
                                    BankStatus status,
                                    Instant creationTimestamp, Instant modificationTimestamp) {
    }

    public record BankMetadata(Integer naicsCode, URL url) {
    }

    public Bank onBankCreated(BankEvent.BankCreated bankCreated) {
        BankCreateInfo createInfo = bankCreated.createInfo();
        return new Bank(createInfo.bankId, createInfo.metadata(),
                createInfo.instanceType, createInfo.users,
                createInfo.status,
                createInfo.creationTimestamp, createInfo.modificationTimestamp);
    }

    public Bank onBankUserAdded(BankEvent.BankUserAdded bankUserAdded) {
        logger.info("Adding user to bank id={} metadata= {}, userId={}", bankId, metadata,bankUserAdded.userId());
        List<String> updatedUsers = Stream.concat(
                Optional.ofNullable(users).stream().flatMap(List::stream),
                Stream.of(bankUserAdded.userId())
        ).toList();

        return new Bank(bankId, metadata, instanceType, updatedUsers,
                BankStatus.BANK_INITIALIZED, creationTimestamp, Instant.now());
    }
}
