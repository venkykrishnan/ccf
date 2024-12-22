package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.Company;
import ccf.domain.CompanyEvent;
import ccf.domain.CompanyInstanceType;
import ccf.domain.CompanyStatus;
import ccf.util.period.FiscalAsOf;
import ccf.util.period.FiscalOperators;
import ccf.util.period.FiscalYearUtils;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;

@ComponentId("company")
public class CompanyEntity extends EventSourcedEntity<Company, CompanyEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(CompanyEntity.class);

    // constructor used to initialize the entityId
    public CompanyEntity(EventSourcedEntityContext context) {
        entityId = context.entityId();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CompanyResult.Success.class, name = "Success"),
            @JsonSubTypes.Type(value = CompanyResult.IncorrectUserId.class, name = "IncorrectUserId")})
    public sealed interface CompanyResult {

        record IncorrectUserId(String message) implements CompanyResult {
        }

        record Success() implements CompanyResult {
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = CompanyPeriodResult.PeriodInstant.class, name = "PeriodInstant"),
            @JsonSubTypes.Type(value = CompanyPeriodResult.PeriodListInstant.class, name = "PeriodListInstant"),
            @JsonSubTypes.Type(value = CompanyPeriodResult.PeriodInteger.class, name = "PeriodListInt"),
            @JsonSubTypes.Type(value = CompanyPeriodResult.IncorrectPeriodOp.class, name = "IncorrectPeriodOp")})
    public sealed interface CompanyPeriodResult {
        record PeriodInstant(Instant instant) implements CompanyPeriodResult {
        }
        record PeriodListInstant(List<Instant> instant) implements CompanyPeriodResult {
        }
        record PeriodInteger(Integer integer) implements CompanyPeriodResult {
        }
        record IncorrectPeriodOp(String message) implements CompanyPeriodResult {
        }
    }

    @Override
    public Company emptyState() {
        return new Company(entityId, null, null, CompanyInstanceType.ACTUAL, null,
                Instant.now(), CompanyStatus.COMPANY_DISABLED,
                Instant.now(), Instant.now());
    }

    public ReadOnlyEffect<Company> getCompany() {
        return effects().reply(currentState());
    }

    public ReadOnlyEffect<CompanyPeriodResult> getPeriodOps(FiscalYearUtils.GetPeriodOpsRequest getPeriodOpsRequest) {
        try {
            Object result = FiscalYearUtils.invokeMethod(getPeriodOpsRequest,
                    currentState().fiscalYears(), currentState().publishedPeriod());
            return switch (result) {
                case Instant instant -> effects().reply(new CompanyPeriodResult.PeriodInstant(instant));
                case Integer i -> effects().reply(new CompanyPeriodResult.PeriodInteger(i));
                case List<?> list -> {
                    if (list.isEmpty() || list.getFirst() instanceof Instant) {
                        @SuppressWarnings("unchecked")
                        List<Instant> instantList = (List<Instant>) list;
                        yield effects().reply(new CompanyPeriodResult.PeriodListInstant(instantList));
                    } else {
                        yield effects().reply(new CompanyPeriodResult.IncorrectPeriodOp("Invalid period operation"));
                    }
                }
                case null, default ->
                        effects().reply(new CompanyPeriodResult.IncorrectPeriodOp("Invalid period operation"));
            };
        } catch (Exception e) {
            logger.error("Invalid period operation: {}", e.getMessage());
            return effects().reply(new CompanyPeriodResult.IncorrectPeriodOp("Invalid period operation"));
        }
    }

    public Effect<Done> createCompany(Company.CompanyMetadata companyMetadata) {
        if (currentState().status() != CompanyStatus.COMPANY_DISABLED) {
            logger.info("Company id={} is already created", entityId);
            return effects().error("Company is already created");
        }

        try {
//            CCFLogger.log(logger,"Create company", Map.of("company_id", entityId, "metadata", companyMetadata.toString()));
//            MDC.put("company_id", entityId);
//            MDC.put("metadata", companyMetadata.toString());
//            logger.info("Creating company");
//            MDC.clear();
            // TODO: Need to get user Id from JWT and populate the user list here.
            Company.CompanyCreateInfo companyCreateInfo = new Company.CompanyCreateInfo(
                    entityId, companyMetadata, CompanyInstanceType.ACTUAL,
                    List.of("vnkAdmin"), Instant.now(),
                    CompanyStatus.COMPANY_INITIALIZED_NO_USERS,
                    Instant.now(), Instant.now()
            );
            var event = new CompanyEvent.CompanyCreated(companyCreateInfo);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            logger.info("Creating company Invalid URL:{} ",e.getMessage());
            return effects().error("Invalid URL");
        }
    }
    public Effect<CompanyResult> addUser(Company.NewUser userInfo) {
        logger.info("Adding user to company id={} userId={}", entityId, userInfo.userId());
        if (currentState().status() == CompanyStatus.COMPANY_DISABLED) {
//            CCFLogger.log(logger,"add user failed as Company not initialized", Map.of("company_id", entityId));
//            logger.info("Company id={} is not an initialized state for adding a user", entityId);
            logger.info("Company id={} is not an initialized state for adding a user", entityId);
            return effects().reply(new CompanyResult.IncorrectUserId("Company is not an initialized state for adding a user"));
        }

        if(currentState().status() == CompanyStatus.COMPANY_INITIALIZED && currentState().users().contains(userInfo.userId())) {
            logger.info("User {} is already added to company id={}", userInfo.userId(), entityId);
            return effects().reply(new CompanyResult.IncorrectUserId("User is already added to company"));
        }

        var event = new CompanyEvent.CompanyUserAdded(userInfo.userId());

        return effects()
                .persist(event)
                .thenReply(newState -> new CompanyResult.Success());
    }
    public Effect<Done> changePublishedPeriod(Instant publishedPeriod) {
        var event = new CompanyEvent.CompanyPublishedPeriodChanged(publishedPeriod);
        return effects()
                .persist(event)
                .thenReply(newState -> Done.getInstance());
    }

    @Override
    public Company applyEvent(CompanyEvent event) {
        return switch (event) {
            case CompanyEvent.CompanyCreated evt -> currentState().onCompanyCreated(evt);
            case CompanyEvent.CompanyUserAdded evt -> currentState().onCompanyUserAdded(evt);
            case CompanyEvent.CompanyPublishedPeriodChanged evt -> currentState().onCompanyPublishedPeriodChanged(evt);
        };
    }

}
