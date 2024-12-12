package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.Company;
import ccf.domain.CompanyEvent;
import ccf.domain.CompanyStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;

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
        return new Company(entityId, null, null, null, null, null, null, null);
    }

    public ReadOnlyEffect<Company> getCompany() {
        return effects().reply(currentState());
    }

    public Effect<Done> createCompany(Company.CompanyMetadata companyMetadata) {
        if (currentState().status() != CompanyStatus.COMPANY_INITIALIZED) {
            logger.info("Company id={} is already created", entityId);
            return effects().error("Company is already created");
        }

//        UrlValidator urlValidator = new UrlValidator();
//        boolean isValidUrl = urlValidator.isValid(companyMetadata.urlString());
//        if (!isValidUrl) {
//            logger.info("Invalid URL: {}", companyMetadata.urlString());
//            return effects().error("Invalid URL");
//        }
        try {

            logger.info("Creating company, metadata={}", companyMetadata);

            // TODO: validate fiscal info

            var event = new CompanyEvent.CompanyCreated(companyMetadata);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            logger.info("Creating company Invalid URL: " + e.getMessage());
            return effects().error("Invalid URL");
        }
    }
    public Effect<CompanyResult> addUser(String userId) {
        if (currentState().status() != CompanyStatus.COMPANY_INITIALIZED) {
            logger.info("Company id={} is not an initialized state for adding a user", entityId);
            return effects().reply(new CompanyResult.IncorrectUserId("Company is not an initialized state for adding a user"));
        }

        if(currentState().users().contains(userId)) {
            logger.info("User {} is already added to company id={}", userId, entityId);
            return effects().reply(new CompanyResult.IncorrectUserId("User is already added to company"));
        }

        var event = new CompanyEvent.CompanyUserAdded(userId);
        return effects()
                .persist(event)
                .thenReply(newState -> new CompanyResult.Success());
    }
    public Effect<Done> changePublishedPeriod(LocalDate publishedPeriod) {
        // TODO validate input
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
