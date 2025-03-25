package ccf.application;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.standard.StandardEvent;
import ccf.domain.standard.TaxonomyRow;
import ccf.domain.standard.TaxonomyRows;

@ComponentId("taxonomy_by_filter")
public class TaxonomyByFilterView extends View {

    @Consume.FromEventSourcedEntity(StandardEntity.class)
    public static class TaxonomyByFilter extends TableUpdater<TaxonomyRow> { // <2>
        public Effect<TaxonomyRow> onEvent(StandardEvent event) { // <3>
            var ret = switch (event) {
                case StandardEvent.StandardTaxonomyAdded added->
                    effects().updateRow(new TaxonomyRow(added.taxonomyCreate().name(),
                    added.taxonomyCreate().description(),
                    
                    added.taxonomyCreate().taxonomyVersions()));
                default -> effects().ignore(); // Add default case to handle other events
            };
            return ret;
        }
    }

    @Query("SELECT * AS taxonomy FROM taxonomy_by_filter")
    public QueryEffect<TaxonomyRows> getAllTaxonomies() {
        return queryResult();
    }
}
