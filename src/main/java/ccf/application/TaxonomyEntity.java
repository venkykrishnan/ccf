package ccf.application;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import ccf.domain.standard.Taxonomy;
import ccf.domain.standard.TaxonomyEvent;
import ccf.domain.standard.TaxonomyStatus;
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
            @JsonSubTypes.Type(value = TaxonomyResult.IncorrectAdd.class, name = "IncorrectAdd")})
    public sealed interface TaxonomyResult {

        record IncorrectAdd(String action,String message) implements TaxonomyResult {
        }

        record Success() implements TaxonomyResult {
        }
    }

    @Override
    public Taxonomy emptyState() {
        return new Taxonomy(entityId, null, null, false, null, null, TaxonomyStatus.TAXONOMY_DISABLED, List.of());
    }

    public ReadOnlyEffect<Taxonomy> getTaxonomy() {
        return effects().reply(currentState());
    }

    public Effect<TaxonomyResult> createTaxonomy(Taxonomy.TaxonomyCreate taxonomyCreate) {
        if (currentState().status() == TaxonomyStatus.TAXONOMY_INITIALIZED) {
            return effects().error("Taxonomy already exists");
        }
        // TODO: Figure out how to check if taxonomy already exists
        try {
            CCFLog.info(logger, "Create taxonomy", Map.of("taxonomy_id", entityId, "metadata", taxonomyCreate.toString()));
            var event = new TaxonomyEvent.TaxonomyCreated(new Taxonomy.TaxonomyCreate(entityId, 
            taxonomyCreate.dimensionName(), taxonomyCreate.name(), taxonomyCreate.description(), taxonomyCreate.version()));
            return effects()
                    .persist(event)
                    .thenReply(newState -> new TaxonomyResult.Success());
        } catch (Exception e) {
            logger.info("Creating bank Invalid URL:{} ",e.getMessage());
            return effects().error("Invalid URL");
        }
    }
    @Override
    public Taxonomy applyEvent(TaxonomyEvent event) {
        return switch (event) {
            case TaxonomyEvent.TaxonomyCreated evt -> currentState().onTaxonomyCreated(evt);
        };
    }

}
