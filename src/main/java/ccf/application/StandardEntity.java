package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.standard.Standard;

import ccf.domain.standard.StandardDomain;
import ccf.domain.standard.StandardEvent;
import ccf.domain.standard.StandardStatus;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@ComponentId("standard")
public class StandardEntity extends EventSourcedEntity<Standard, StandardEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(StandardEntity.class);

    // constructor used to initialize the entityId
    public StandardEntity(EventSourcedEntityContext context) {
        entityId = context.entityId();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = StandardResult.Success.class, name = "Success"),
            @JsonSubTypes.Type(value = StandardResult.IncorrectAdd.class, name = "IncorrectAdd")})
    public sealed interface StandardResult {
        record IncorrectAdd(String inputType, String message) implements StandardResult {
        }
        record Success() implements StandardResult {
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
    public Standard emptyState() {
        return new Standard(entityId, null, null, null, StandardStatus.STANDARD_DISABLED);
    }

    public ReadOnlyEffect<Standard> getStandard() {
        return effects().reply(currentState());
    }

    public Effect<Done> createStandard(Standard.StandardCreate standardCreate) {
        if (currentState().status() != StandardStatus.STANDARD_DISABLED) {
            CCFLog.error(logger, "Creating standard failed, already created",
                    Map.of("name", standardCreate.name()));
            logger.info("Standard id={} is already created", entityId);
            return effects().error("Company is already created");
        }

        try {
            var event = new StandardEvent.StandardCreated(standardCreate);
            return effects()
                    .persist(event)
                    .thenReply(newState -> Done.getInstance());
        } catch (Exception e) {
            CCFLog.error(logger, "Creating standard failed, already created",
                    Map.of("name", standardCreate.name(),
                            "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    // HIA: 20 March 2025 - working on addDomain (how to handle exceptions)

    public Effect<StandardResult> addDomain(StandardDomain standardDomain) {
        CCFLog.info(logger, "Adding domain", Map.of("standard", entityId, "domain", standardDomain.name()));
        if (currentState().status() == StandardStatus.STANDARD_DISABLED) {
            CCFLog.error(logger, "Adding domain failed as standard is disabled", Map.of("standard", entityId, "domain", standardDomain.name()));
            return effects().reply(new StandardResult.IncorrectAdd("addDomain", "failed as standard %s is disabled".formatted(standardDomain.name())));
        }

        var event = new StandardEvent.StandardDomainAdded(standardDomain);

        return effects()
                .persist(event)
                .thenReply(newState -> new StandardResult.Success());
    }

    @Override
    public Standard applyEvent(StandardEvent event) {
        return switch (event) {
            //<editor-fold desc="Create & Modify">
            case StandardEvent.StandardCreated evt -> currentState().onStandardCreated(evt);
            case StandardEvent.StandardDomainAdded evt -> currentState().onStandardDomainAdded(evt);
            case StandardEvent.StandardDimensionAdded evt -> currentState().onStandardDimensionAdded(evt);
            case StandardEvent.StandardTaxonomyAdded evt -> currentState().onStandardTaxonomyAdded(evt);
            case StandardEvent.StandardTaxonomyVersionAdded evt -> currentState().onStandardTaxonomyVersionAdded(evt);
            case StandardEvent.StandardDimensionRowsAdded evt -> currentState().onStandardDimensionRowsAdded(evt);
            case StandardEvent.StandardDimensionRowAdded evt -> currentState().onStandardDimensionRowAdded(evt);
            case StandardEvent.StandardTaxonomyDefaultVersionSet evt -> currentState().onStandardTaxonomyDefaultVersionSet(evt);
            case StandardEvent.StandardTaxonomyPublish evt -> currentState().onStandardTaxonomyPublished(evt);
            //</editor-fold>

            //<editor-fold desc="Clean up">
            case StandardEvent.StandardDimensionRowRemoved evt -> currentState().onStandardDimensionRowRemoved(evt);
            case StandardEvent.StandardDimensionRowsRemoved evt -> currentState().onStandardDimensionRowsRemoved(evt);
            case StandardEvent.StandardTaxonomyVersionRemoved evt -> currentState().onStandardTaxonomyVersionRemoved(evt);
            case StandardEvent.StandardTaxonomyRemoved evt -> currentState().onStandardTaxonomyRemoved(evt);
            case StandardEvent.StandardDimensionRemoved evt -> currentState().onStandardDimensionRemoved(evt);
            case StandardEvent.StandardDomainRemoved evt -> currentState().onStandardDomainRemoved(evt);
            //</editor-fold>

        };
    }

}
