package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.standard.*;

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

    public Effect<StandardResult> addDomain(StandardDomain standardDomain) {
        try {
        CCFLog.info(logger, "Adding domain", Map.of("standard", entityId, "domain", standardDomain.name()));
        if (currentState().status() == StandardStatus.STANDARD_DISABLED) {
            CCFLog.error(logger, "Adding domain failed as standard is disabled", Map.of("standard", entityId, "domain", standardDomain.name()));
            return effects().reply(new StandardResult.IncorrectAdd("addDomain", "failed as standard %s is disabled".formatted(standardDomain.name())));
        }

        var event = new StandardEvent.StandardDomainAdded(standardDomain);

        return effects()
                .persist(event)
                .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding domain failed", Map.of("standard", entityId, "domain", standardDomain.name(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("addDomain", ie.getMessage()));
        }
        catch (Exception e) {
            CCFLog.error(logger, "Adding domain failed", Map.of("standard", entityId, "domain", standardDomain.name(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> addDimension(StandardDimension.StandardDimensionCreate standardDimensionCreate) {
        try {
            CCFLog.info(logger, "Adding dimension", Map.of("standard", entityId, "dimension", standardDimensionCreate.name()));
            if (currentState().status() == StandardStatus.STANDARD_INITIALIZED_NO_DOMAINS) {
                CCFLog.error(logger, "Adding dimension failed as standard is disabled", Map.of("standard", entityId, "dimension", standardDimensionCreate.name()));
                return effects().reply(new StandardResult.IncorrectAdd("addDimension", "failed as standard %s is disabled".formatted(standardDimensionCreate.name())));
            }
            var event = new StandardEvent.StandardDimensionAdded(standardDimensionCreate);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding dimension failed", Map.of("standard", entityId, "dimension", standardDimensionCreate.name(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("addDimension", ie.getMessage()));
        }
        catch (Exception e) {
            CCFLog.error(logger, "Adding dimension failed", Map.of("standard", entityId, "dimension", standardDimensionCreate.name(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> addTaxonomy(StandardDimension.TaxonomyCreate taxonomyCreate) {
        try {
            CCFLog.info(logger, "Adding taxonomy", Map.of("standard", entityId, "taxonomy", taxonomyCreate.name()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Adding taxonomy failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyCreate.name()));
                return effects().reply(new StandardResult.IncorrectAdd("addTaxonomy", "failed as standard %s is disabled".formatted(taxonomyCreate.name())));
            }
            var event = new StandardEvent.StandardTaxonomyAdded(taxonomyCreate);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyCreate.name(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("addTaxonomy", ie.getMessage()));
        }
        catch (Exception e) {
            CCFLog.error(logger, "Adding taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyCreate.name(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> addTaxonomyVersion(StandardDimension.TaxonomyVersionCreate taxonomyVersionCreate) {
        try {
            CCFLog.info(logger, "Adding taxonomy version", Map.of("standard", entityId, "taxonomy", taxonomyVersionCreate.taxonomyName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Adding taxonomy version failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyVersionCreate.taxonomyName()));
                return effects().reply(new StandardResult.IncorrectAdd("addTaxonomyVersion", "failed as standard %s is disabled".formatted(taxonomyVersionCreate.taxonomyName())));
            }
            var event = new StandardEvent.StandardTaxonomyVersionAdded(taxonomyVersionCreate);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding taxonomy version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionCreate.taxonomyName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("addTaxonomyVersion", ie.getMessage()));
        }
        catch (Exception e) {
            CCFLog.error(logger, "Adding taxonomy version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionCreate.taxonomyName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> addDimensionRow(StandardDimension.DimensionRowAdd rowAdd) {
        try {
            CCFLog.info(logger, "Adding dimension row", Map.of("standard", entityId, "dimension", rowAdd.dimensionName(), "taxonomy", rowAdd.taxonomyName(), "version", rowAdd.versionName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Adding dimension row failed as standard is disabled", Map.of("standard", entityId, "dimension", rowAdd.dimensionName(), "taxonomy", rowAdd.taxonomyName(), "version", rowAdd.versionName()));
                return effects().reply(new StandardResult.IncorrectAdd("addDimensionRow", "failed as standard %s is disabled".formatted(rowAdd.dimensionName())));
            }
            var event = new StandardEvent.StandardDimensionRowAdded(rowAdd);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding dimension row failed", Map.of("standard", entityId, "dimension", rowAdd.dimensionName(), "taxonomy", rowAdd.taxonomyName(), "version", rowAdd.versionName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("addDimensionRow", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Adding dimension row failed", Map.of("standard", entityId, "dimension", rowAdd.dimensionName(), "taxonomy", rowAdd.taxonomyName(), "version", rowAdd.versionName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> addDimensionRows(StandardDimension.DimensionRowsAdd rowsAdd) {
        try {
            CCFLog.info(logger, "Adding dimension rows", Map.of("standard", entityId, "dimension", rowsAdd.dimensionName(), "taxonomy", rowsAdd.taxonomyName(), "version", rowsAdd.versionName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Adding dimension rows failed as standard is disabled", Map.of("standard", entityId, "dimension", rowsAdd.dimensionName(), "taxonomy", rowsAdd.taxonomyName(), "version", rowsAdd.versionName()));
                return effects().reply(new StandardResult.IncorrectAdd("addDimensionRows", "failed as standard %s is disabled".formatted(rowsAdd.dimensionName())));
            }
            var event = new StandardEvent.StandardDimensionRowsAdded(rowsAdd);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Adding dimension rows failed", Map.of("standard", entityId, "dimension", rowsAdd.dimensionName(), "taxonomy", rowsAdd.taxonomyName(), "version", rowsAdd.versionName()));
            return effects().reply(new StandardResult.IncorrectAdd("addDimensionRows", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Adding dimension rows failed", Map.of("standard", entityId, "dimension", rowsAdd.dimensionName(), "taxonomy", rowsAdd.taxonomyName(), "version", rowsAdd.versionName()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> setTaxonomyDefaultVersion(StandardDimension.TaxonomyVersionDefault  taxonomyVersionDefault) {
        try {
            CCFLog.info(logger, "Setting taxonomy default version", Map.of("standard", entityId, "taxonomy", taxonomyVersionDefault.taxonomyName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Setting taxonomy default version failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyVersionDefault.taxonomyName()));
                return effects().reply(new StandardResult.IncorrectAdd("setTaxonomyDefaultVersion", "failed as standard %s is disabled".formatted(taxonomyVersionDefault.taxonomyName())));
            }
            var event = new StandardEvent.StandardTaxonomyDefaultVersionSet(taxonomyVersionDefault);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Setting taxonomy default version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionDefault.taxonomyName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("setTaxonomyDefaultVersion", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Setting taxonomy default version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionDefault.taxonomyName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> publishTaxonomy(StandardDimension.TaxonomyVersionPublish taxonomyVersionPublish) {
        try {
            CCFLog.info(logger, "Publishing taxonomy", Map.of("standard", entityId, "taxonomy", taxonomyVersionPublish.taxonomyName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Publishing taxonomy failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyVersionPublish.taxonomyName()));
                return effects().reply(new StandardResult.IncorrectAdd("publishTaxonomy", "failed as standard %s is disabled".formatted(taxonomyVersionPublish.taxonomyName())));
            }
            var event = new StandardEvent.StandardTaxonomyPublish(taxonomyVersionPublish);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Publishing taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionPublish.taxonomyName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("publishTaxonomy", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Publishing taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionPublish.taxonomyName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> removeDimensionRow(StandardDimension.DimensionRowRemove dimensionRowRemove) {
        try {
            CCFLog.info(logger, "Removing dimension row", Map.of("standard", entityId, "dimension", dimensionRowRemove.dimensionName(), "taxonomy", dimensionRowRemove.taxonomyName(), "version", dimensionRowRemove.versionName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing dimension row failed as standard is disabled", Map.of("standard", entityId, "dimension", dimensionRowRemove.dimensionName(), "taxonomy", dimensionRowRemove.taxonomyName(), "version", dimensionRowRemove.versionName()));
                return effects().reply(new StandardResult.IncorrectAdd("removeDimensionRow", "failed as standard %s is disabled".formatted(dimensionRowRemove.dimensionName())));
            }
            var event = new StandardEvent.StandardDimensionRowRemoved(dimensionRowRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing dimension row failed", Map.of("standard", entityId, "dimension", dimensionRowRemove.dimensionName(), "taxonomy", dimensionRowRemove.taxonomyName(), "version", dimensionRowRemove.versionName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeDimensionRow", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing dimension row failed", Map.of("standard", entityId, "dimension", dimensionRowRemove.dimensionName(), "taxonomy", dimensionRowRemove.taxonomyName(), "version", dimensionRowRemove.versionName(), "error", e.getMessage()));
            return effects().error(e.getMessage());

        }
    }

    public Effect<StandardResult> removeDimensionRows(StandardDimension.DimensionRowsRemove dimensionRowsRemove) {
        try {
            CCFLog.info(logger, "Removing dimension rows", Map.of("standard", entityId, "dimension", dimensionRowsRemove.dimensionName(), "taxonomy", dimensionRowsRemove.taxonomyName(), "version", dimensionRowsRemove.version().version()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing dimension rows failed as standard is disabled", Map.of("standard", entityId, "dimension", dimensionRowsRemove.dimensionName(), "taxonomy", dimensionRowsRemove.taxonomyName(), "version", dimensionRowsRemove.version().version()));
                return effects().reply(new StandardResult.IncorrectAdd("removeDimensionRows", "failed as standard %s is disabled".formatted(dimensionRowsRemove.dimensionName())));
            }
            var event = new StandardEvent.StandardDimensionRowsRemoved(dimensionRowsRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing dimension rows failed", Map.of("standard", entityId, "dimension", dimensionRowsRemove.dimensionName(), "taxonomy", dimensionRowsRemove.taxonomyName(), "version", dimensionRowsRemove.version().version(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeDimensionRows", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing dimension rows failed", Map.of("standard", entityId, "dimension", dimensionRowsRemove.dimensionName(), "taxonomy", dimensionRowsRemove.taxonomyName(), "version", dimensionRowsRemove.version().version(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> removeTaxonomyVersion(StandardDimension.TaxonomyVersionRemove taxonomyVersionRemove) {
        try {
            CCFLog.info(logger, "Removing taxonomy version", Map.of("standard", entityId, "taxonomy", taxonomyVersionRemove.taxonomyName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing taxonomy version failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyVersionRemove.taxonomyName()));
                return effects().reply(new StandardResult.IncorrectAdd("removeTaxonomyVersion", "failed as standard %s is disabled".formatted(taxonomyVersionRemove.taxonomyName())));
            }
            var event = new StandardEvent.StandardTaxonomyVersionRemoved(taxonomyVersionRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing taxonomy version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionRemove.taxonomyName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeTaxonomyVersion", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing taxonomy version failed", Map.of("standard", entityId, "taxonomy", taxonomyVersionRemove.taxonomyName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> removeTaxonomy(StandardDimension.TaxonomyRemove taxonomyRemove) {
        try {
            CCFLog.info(logger, "Removing taxonomy", Map.of("standard", entityId, "taxonomy", taxonomyRemove.taxonomyName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing taxonomy failed as standard is disabled", Map.of("standard", entityId, "taxonomy", taxonomyRemove.taxonomyName()));
                return effects().reply(new StandardResult.IncorrectAdd("removeTaxonomy", "failed as standard %s is disabled".formatted(taxonomyRemove.taxonomyName())));
            }
            var event = new StandardEvent.StandardTaxonomyRemoved(taxonomyRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyRemove.taxonomyName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeTaxonomy", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing taxonomy failed", Map.of("standard", entityId, "taxonomy", taxonomyRemove.taxonomyName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> removeDimension(StandardDimension.DimensionRemove dimensionRemove) {
        try {
            CCFLog.info(logger, "Removing dimension", Map.of("standard", entityId, "dimension", dimensionRemove.dimensionName()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing dimension failed as standard is disabled", Map.of("standard", entityId, "dimension", dimensionRemove.dimensionName()));
                return effects().reply(new StandardResult.IncorrectAdd("removeDimension", "failed as standard %s is disabled".formatted(dimensionRemove.dimensionName())));
            }
            var event = new StandardEvent.StandardDimensionRemoved(dimensionRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing dimension failed", Map.of("standard", entityId, "dimension", dimensionRemove.dimensionName(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeDimension", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing dimension failed", Map.of("standard", entityId, "dimension", dimensionRemove.dimensionName(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }
    }

    public Effect<StandardResult> removeDomain(StandardDomain.DomainRemove domainRemove) {
        try {
            CCFLog.info(logger, "Removing domain", Map.of("standard", entityId, "domain", domainRemove.name()));
            if (currentState().status() != StandardStatus.STANDARD_INITIALIZED) {
                CCFLog.error(logger, "Removing domain failed as standard is disabled", Map.of("standard", entityId, "domain", domainRemove.name()));
                return effects().reply(new StandardResult.IncorrectAdd("removeDomain", "failed as standard %s is disabled".formatted(domainRemove.name())));
            }
            var event = new StandardEvent.StandardDomainRemoved(domainRemove);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new StandardResult.Success());
        } catch (IllegalArgumentException ie) {
            CCFLog.error(logger, "Removing domain failed", Map.of("standard", entityId, "domain", domainRemove.name(), "error", ie.getMessage()));
            return effects().reply(new StandardResult.IncorrectAdd("removeDomain", ie.getMessage()));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing domain failed", Map.of("standard", entityId, "domain", domainRemove.name(), "error", e.getMessage()));
            return effects().error(e.getMessage());
        }   
    }

    

    @Override
    public Standard applyEvent(StandardEvent event) {
        return switch (event) {
            //<editor-fold desc="Create & Modify">
            case StandardEvent.StandardCreated evt -> currentState().onStandardCreated(evt);
            case StandardEvent.StandardDomainAdded evt -> {
                try {
                    yield currentState().onStandardDomainAdded(evt);
                } catch (StandardException e) {
                    throw new RuntimeException(e);
                }
            }
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
