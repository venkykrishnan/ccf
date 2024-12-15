package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.Company;
import ccf.domain.CompanyEvent;
import ccf.domain.CompanyStatus;
import ccf.util.CCFLogger;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.Map;

@ComponentId("company")
public class CompanyEntity extends EventSourcedEntity<Company, CompanyEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(CompanyEntity.class);

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


    @Override
    public Company emptyState() {
        return new Company(entityId, null, null, null, null, CompanyStatus.COMPANY_DISABLED, null, null);
    }

    public ReadOnlyEffect<Company> getCompany() {
        return effects().reply(currentState());
    }

    public Effect<Done> createCompany(Company.CompanyMetadata companyMetadata) {
        if (currentState().status() != CompanyStatus.COMPANY_DISABLED) {
            logger.info("Company id={} is already created", entityId);
            return effects().error("Company is already created");
        }

        try {
            CCFLogger.log(logger,"Create company", Map.of("company_id", entityId, "metadata", companyMetadata.toString()));
//            MDC.put("company_id", entityId);
//            MDC.put("metadata", companyMetadata.toString());
//            logger.info("Creating company");
//            MDC.clear();

            var event = new CompanyEvent.CompanyCreated(companyMetadata);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            logger.info("Creating company Invalid URL: " + e.getMessage());
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
        logger.info("Before check for contains (1)");

        if(currentState().status() == CompanyStatus.COMPANY_INITIALIZED && currentState().users().contains(userInfo.userId())) {
            logger.info("User {} is already added to company id={}", userInfo.userId(), entityId);
            return effects().reply(new CompanyResult.IncorrectUserId("User is already added to company"));
        }
        logger.info("After check for contains (2)");

        var event = new CompanyEvent.CompanyUserAdded(userInfo.userId());
        logger.info("After creating an event (3)");

        return effects()
                .persist(event)
                .thenReply(newState -> new CompanyResult.Success());
    }
    public Effect<Done> changePublishedPeriod(Company.PublishedPeriod publishedPeriod) {
        var event = new CompanyEvent.CompanyPublishedPeriodChanged(publishedPeriod.publishedPeriod());
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
