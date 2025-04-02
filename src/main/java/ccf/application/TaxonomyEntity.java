package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.standard.Taxonomy;
import ccf.domain.standard.TaxonomyEvent;
import ccf.domain.standard.TaxonomyStatus;
import ccf.domain.standard.Taxonomy.TaxonomyCreate;
import ccf.util.CCFLog;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ComponentId("taxonomy")
public class TaxonomyEntity extends EventSourcedEntity<Taxonomy, TaxonomyEvent> {

    private final String entityId;
    private final Logger logger = LoggerFactory.getLogger(TaxonomyEntity.class);

    // constructor used to initialize the entityId
    public TaxonomyEntity(EventSourcedEntityContext context) {
        entityId = context.entityId();
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TaxonomyResult.Success.class, name = "Success"),
            @JsonSubTypes.Type(value = TaxonomyResult.GetFailed.class, name = "GetFailed"),
            @JsonSubTypes.Type(value = TaxonomyResult.CreateFailed.class, name = "CreateFailed"),
            @JsonSubTypes.Type(value = TaxonomyResult.RemoveFailed.class, name = "RemoveFailed"),
            @JsonSubTypes.Type(value = TaxonomyResult.IncorrectAdd.class, name = "IncorrectAdd"),
            @JsonSubTypes.Type(value = TaxonomyResult.PublishFailed.class, name = "PublishFailed"),
            @JsonSubTypes.Type(value = TaxonomyResult.IncorrectRemove.class, name = "IncorrectRemove"),
            @JsonSubTypes.Type(value = TaxonomyResult.IncorrectUpate.class, name = "IncorrectUpate")})
    public sealed interface TaxonomyResult {

        record CreateFailed(String message) implements TaxonomyResult {
        }

        record GetFailed(String message) implements TaxonomyResult {
        }

        record IncorrectAdd(String action,String message) implements TaxonomyResult {
        }

        record RemoveFailed(String message) implements TaxonomyResult {
        }

        record Success(String id) implements TaxonomyResult {
        }

        record PublishFailed(String message) implements TaxonomyResult {
        }

        record IncorrectRemove(String action, String message) implements TaxonomyResult {
        }

        record IncorrectUpate(String message) implements TaxonomyResult {
        }
    }

    @Override
    public Taxonomy emptyState() {
        return new Taxonomy(entityId, null, null, null, null, TaxonomyStatus.TAXONOMY_DISABLED, List.of());
    }

    public ReadOnlyEffect<TaxonomyResult> getTaxonomy() {
        try {
            CCFLog.info(logger, "getTaxonomy",
                    Map.of("taxonomy_id", entityId, "status", currentState().status().toString()));
            if (currentState().status() == TaxonomyStatus.TAXONOMY_DISABLED) {
                CCFLog.debug(logger, "Taxonomy is disabled", Map.of("taxonomy_id", entityId));
                return effects().reply(new TaxonomyResult.GetFailed("Taxonomy %s doesnt exist".formatted(entityId)));
            }
            CCFLog.debug(logger, "Taxonomy is enabled", Map.of("taxonomy_id", entityId));
            return effects().reply(new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "getTaxonomy failed", Map.of("taxonomy_id", entityId, "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.GetFailed(e.getMessage()));
        }

        // return effects().reply(currentState());
    }

    public Effect<TaxonomyResult> createTaxonomy(TaxonomyCreate taxonomyCreate) {
        try {
            CCFLog.info(logger, "Create taxonomy",
                    Map.of("taxonomy_id", entityId, "taxonomyCreate", taxonomyCreate.toString()));
            var event = new TaxonomyEvent.TaxonomyCreated(taxonomyCreate);
            return effects()
                    .persist(event)
                    .thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Creating Taxonomy failed", Map.of("taxonomy_id", entityId, "taxonomyCreate",
                    taxonomyCreate.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.CreateFailed(e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> removeTaxonomy() {
        try {
            CCFLog.info(logger, "Remove taxonomy", Map.of("taxonomy_id", entityId));
            var event = new TaxonomyEvent.TaxonomyRemoved();
            return effects()
                .persist(event)
            .thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing Taxonomy failed", Map.of("taxonomy_id", entityId, "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.RemoveFailed(e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> publishTaxonomy(Boolean taxonomyPublish) {
        try {
            CCFLog.info(logger, "Publish taxonomy", Map.of("taxonomy_id", entityId, "taxonomyPublish", taxonomyPublish.toString()));
            var event = new TaxonomyEvent.TaxonomyPublished(taxonomyPublish);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Publishing Taxonomy failed", Map.of("taxonomy_id", entityId, "taxonomyPublish",
                    taxonomyPublish.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.PublishFailed(e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> addTaxRow(Taxonomy.TaxRowAdd taxRowAdd) {
        try {
            CCFLog.info(logger, "Add tax row", Map.of("taxonomy_id", entityId, "taxRowAdd", taxRowAdd.toString()));
            var event = new TaxonomyEvent.TaxonomyTaxRowAdded(taxRowAdd);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Adding Taxonomy Tax Row failed", Map.of("taxonomy_id", entityId, "taxRowAdd",
                    taxRowAdd.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.IncorrectAdd("addTaxRow", e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> removeTaxRow(String rowId) {
        try {
            CCFLog.info(logger, "Remove tax row", Map.of("taxonomy_id", entityId, "rowId", rowId));
            var event = new TaxonomyEvent.TaxonomyTaxRowRemoved(rowId);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing Taxonomy Tax Row failed", Map.of("taxonomy_id", entityId, "rowId", rowId, "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.IncorrectRemove("removeTaxRow", e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> addTaxRows(Taxonomy.TaxRowsAdd taxRowsAdd) {
        try {
            CCFLog.info(logger, "Add tax rows", Map.of("taxonomy_id", entityId, "taxRowsAdd", taxRowsAdd.toString()));
            var event = new TaxonomyEvent.TaxonomyTaxRowsAdded(taxRowsAdd);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Adding Taxonomy Tax Rows failed", Map.of("taxonomy_id", entityId, "taxRowsAdd",
                    taxRowsAdd.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.IncorrectAdd("addTaxRows", e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> removeTaxRows(Taxonomy.TaxRowsRemove taxRowsRemove) {
        try {
            CCFLog.info(logger, "Remove tax rows", Map.of("taxonomy_id", entityId, "taxRowsRemove", taxRowsRemove.toString()));
            var event = new TaxonomyEvent.TaxonomyTaxRowsRemoved(taxRowsRemove);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Removing Taxonomy Tax Rows failed", Map.of("taxonomy_id", entityId, "taxRowsRemove",
                    taxRowsRemove.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.IncorrectRemove("removeTaxRows", e.getMessage()));
        }
    }

    public Effect<TaxonomyResult> updateTaxRow(Taxonomy.TaxRowUpdate taxRowUpdate) {
        try {
            CCFLog.info(logger, "Update tax row", Map.of("taxonomy_id", entityId, "taxRowUpdate", taxRowUpdate.toString()));
            var event = new TaxonomyEvent.TaxonomyTaxRowUpdated(taxRowUpdate);
            return effects().persist(event).thenReply(newState -> new TaxonomyResult.Success(entityId));
        } catch (Exception e) {
            CCFLog.error(logger, "Updating Taxonomy Tax Row failed", Map.of("taxonomy_id", entityId, "taxRowUpdate",
                    taxRowUpdate.toString(), "error", e.getMessage()));
            return effects().reply(new TaxonomyResult.IncorrectUpate(e.getMessage()));
        }
    }   

    @Override
    public Taxonomy applyEvent(TaxonomyEvent event) {
        return switch (event) {
            case TaxonomyEvent.TaxonomyCreated evt -> currentState().onTaxonomyCreated(evt);
            case TaxonomyEvent.TaxonomyRemoved evt -> currentState().onTaxonomyRemoved(evt);
            case TaxonomyEvent.TaxonomyPublished evt -> currentState().onTaxonomyPublished(evt);
            case TaxonomyEvent.TaxonomyTaxRowAdded evt -> currentState().onTaxonomyTaxRowAdded(evt);
            case TaxonomyEvent.TaxonomyTaxRowsAdded evt -> currentState().onTaxonomyTaxRowsAdded(evt);
            case TaxonomyEvent.TaxonomyTaxRowRemoved evt -> currentState().onTaxonomyTaxRowRemoved(evt);
            case TaxonomyEvent.TaxonomyTaxRowsRemoved evt -> currentState().onTaxonomyTaxRowsRemoved(evt);
            case TaxonomyEvent.TaxonomyTaxRowUpdated evt -> currentState().onTaxonomyTaxRowUpdated(evt);
            default -> currentState();
        };
    }

}
