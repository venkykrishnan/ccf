package ccf.application;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.standard.TaxonomyEvent;
import ccf.domain.standard.TaxonomyRow;
import ccf.domain.standard.Taxonomys;
import ccf.util.CCFLog;

@ComponentId("taxonomy_by_filter")
public class TaxonomyByFilterView extends View {

    @Consume.FromEventSourcedEntity(TaxonomyEntity.class)
    public static class TaxonomyByFilter extends TableUpdater<TaxonomyRow> { // <2>
        private static final Logger logger = LoggerFactory.getLogger(TaxonomyByFilter.class);
        public Effect<TaxonomyRow> onEvent(TaxonomyEvent event) { // <3>
            var ret = switch (event) {
                case TaxonomyEvent.TaxonomyCreated created -> {
                    CCFLog.info(logger, "Taxonomy created in view", Map.of("taxonomy", created.taxonomyCreate().toString()));
                    var taxonomy = new TaxonomyRow(created.taxonomyCreate().name(), created.taxonomyCreate().description(), 
                            created.taxonomyCreate().version(), List.<TaxonomyRow.TRRow>of(), false);
                    CCFLog.info(logger, "Taxonomy created in view - post new TaxonomyRow", Map.of("taxonomy", taxonomy.toString()));
                    yield effects().updateRow(taxonomy);
                }
                case TaxonomyEvent.TaxonomyRemoved removed -> {
                    CCFLog.info(logger, "Taxonomy removed in view", Map.of("taxonomy", removed.toString())); 
                    yield effects().deleteRow();
                }
                case TaxonomyEvent.TaxonomyPublished published ->
                    effects().updateRow(rowState().onTaxonomyPublished(published.isPublish()));
                case TaxonomyEvent.TaxonomyTaxRowAdded added -> effects().updateRow(rowState().onTaxonomyTaxRowAdded(added.taxRowAdd()));
                case TaxonomyEvent.TaxonomyTaxRowsAdded added -> effects().updateRow(rowState().onTaxonomyTaxRowsAdded(added.taxRowsAdd()));
                case TaxonomyEvent.TaxonomyTaxRowRemoved removed -> effects().updateRow(rowState().onTaxonomyTaxRowRemoved(removed.rowId()));
                case TaxonomyEvent.TaxonomyTaxRowsRemoved removed -> effects().updateRow(rowState().onTaxonomyTaxRowsRemoved(removed.taxRowsRemove()));
                case TaxonomyEvent.TaxonomyTaxRowUpdated updated -> effects().updateRow(rowState().onTaxonomyTaxRowUpdated(updated.taxRowUpdate()));
                default -> effects().updateRow(rowState());
            };
            return ret;
        }
    }

    @Query("SELECT * AS taxonomies FROM taxonomy_by_filter")
    public QueryEffect<Taxonomys> getAllTaxonomies() {
        return queryResult();
    }

    // Note: This compiled & didnt cause runtime error, but i never used/tested it.
    // @Query("SELECT * AS taxonomies FROM taxonomy_by_filter WHERE dimension = :dimensionName")
    // public QueryEffect<Taxonomys> getTaxonomiesByDimension(String dimensionName) {
    //     return queryResult();
    // }

    
    // @Query("SELECT * AS taxonomy FROM taxonomy_by_filter WHERE taxonomy.published = true AND taxonomy.dimensionName = :dimensionAndName.dimension AND taxonomy.name = :dimensionAndName.name")
    // public QueryEffect<TaxonomyRows> getPublishedTaxonomyByDimensionAndName(TaxonomyByDimensionAndName dimensionAndName) {
    //     return queryResult();
    // }

    // @Query("SELECT rows AS nodeRows FROM taxonomy_by_filter WHERE taxonomy.name = :taxonomyId")
    // public QueryEffect<Nodes> getNodesByTaxonomyId(String taxonomyId) {
    //     return queryResult();
    // }


}
