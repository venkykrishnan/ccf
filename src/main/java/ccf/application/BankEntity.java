package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.bank.Bank;
import ccf.domain.bank.BankEvent;
import ccf.domain.bank.BankInstanceType;
import ccf.domain.bank.BankStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@ComponentId("bank")
public class BankEntity extends EventSourcedEntity<Bank, BankEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(BankEntity.class);

    // constructor used to initialize the entityId
    public BankEntity(EventSourcedEntityContext context) {
        entityId = context.entityId();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = BankResult.Success.class, name = "Success"),
            @JsonSubTypes.Type(value = BankResult.IncorrectUserId.class, name = "IncorrectUserId")})
    public sealed interface BankResult {

        record IncorrectUserId(String message) implements BankResult {
        }

        record Success() implements BankResult {
        }
    }

    @Override
    public Bank emptyState() {
        return new Bank(entityId, null, BankInstanceType.BANK_INSTANCE_TYPE_ACTUAL, null,
                BankStatus.BANK_DISABLED, Instant.now(), Instant.now());
    }

    public ReadOnlyEffect<Bank> getBank() {
        return effects().reply(currentState());
    }

    public Effect<Done> createBank(Bank.BankMetadata bankMetadata) {
        if (currentState().status() != BankStatus.BANK_DISABLED) {
            logger.info("Bank id={} is already created", entityId);
            return effects().error("Bank is already created");
        }

        try {
//            CCFLogger.log(logger,"Create company", Map.of("company_id", entityId, "metadata", companyMetadata.toString()));
//            MDC.put("company_id", entityId);
//            MDC.put("metadata", companyMetadata.toString());
//            logger.info("Creating company");
//            MDC.clear();
            // TODO: Need to get user Id from JWT and populate the user list here.
            Bank.BankCreateInfo bankCreateInfo = new Bank.BankCreateInfo(
                    entityId, bankMetadata, BankInstanceType.BANK_INSTANCE_TYPE_ACTUAL,
                    List.of("vnkAdmin"),
                    BankStatus.BANK_INITIALIZED_NO_USERS,
                    Instant.now(), Instant.now()
            );
            var event = new BankEvent.BankCreated(bankCreateInfo);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            logger.info("Creating bank Invalid URL:{} ",e.getMessage());
            return effects().error("Invalid URL");
        }
    }
    public Effect<BankResult> addUser(Bank.NewUser userInfo) {
        logger.info("Adding user to bank id={} userId={}", entityId, userInfo.userId());
        if (currentState().status() == BankStatus.BANK_DISABLED) {
//            CCFLogger.log(logger,"add user failed as Company not initialized", Map.of("company_id", entityId));
//            logger.info("Company id={} is not an initialized state for adding a user", entityId);
            logger.info("Bank id={} is not an initialized state for adding a user", entityId);
            return effects().reply(new BankResult.IncorrectUserId("Bank is not an initialized state for adding a user"));
        }

        if(currentState().status() == BankStatus.BANK_INITIALIZED && currentState().users().contains(userInfo.userId())) {
            logger.info("User {} is already added to bank id={}", userInfo.userId(), entityId);
            return effects().reply(new BankResult.IncorrectUserId("User is already added to bank"));
        }

        var event = new BankEvent.BankUserAdded(userInfo.userId());

        return effects()
                .persist(event)
                .thenReply(newState -> new BankResult.Success());
    }

    @Override
    public Bank applyEvent(BankEvent event) {
        return switch (event) {
            case BankEvent.BankCreated evt -> currentState().onBankCreated(evt);
            case BankEvent.BankUserAdded evt -> currentState().onBankUserAdded(evt);
        };
    }

}
