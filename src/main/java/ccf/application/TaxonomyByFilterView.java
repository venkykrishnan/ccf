package ccf.application;

import java.util.List;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.standard.NodeRow;
import ccf.domain.standard.NodeRows;
import ccf.domain.standard.TaxonomyEvent;
import ccf.domain.standard.TaxonomyRow;
import ccf.domain.standard.TaxonomyRows;

@ComponentId("taxonomy_by_filter")
public class TaxonomyByFilterView extends View {

    @Consume.FromEventSourcedEntity(TaxonomyEntity.class)
    public static class TaxonomyByFilter extends TableUpdater<TaxonomyRow> { // <2>
        public Effect<TaxonomyRow> onEvent(TaxonomyEvent event) { // <3>
            var ret = switch (event) {
                case TaxonomyEvent.TaxonomyCreated created -> effects().updateRow(
                        new TaxonomyRow(created.taxonomyCreate().name(), created.taxonomyCreate().description(),
                                created.taxonomyCreate().version(),
                                created.taxonomyCreate().dimensionName(), List.<TaxonomyRow.Nodes>of(), false));
                case TaxonomyEvent.TaxonomyRemoved removed -> effects().deleteRow();
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

    @Query("SELECT * AS taxonomy FROM taxonomy_by_filter")
    public QueryEffect<TaxonomyRows> getAllTaxonomies() {
        return queryResult();
    }

    @Query("SELECT * AS taxonomy FROM taxonomy_by_filter WHERE taxonomy.dimensionName = :dimensionName")
    public QueryEffect<TaxonomyRows> getTaxonomiesByDimension(String dimensionName) {
        return queryResult();
    }

    @Query("SELECT * AS taxonomy FROM taxonomy_by_filter WHERE taxonomy.dimensionName = :dimensionName AND taxonomy.name = :name")
    public QueryEffect<TaxonomyRows> getTaxonomyByDimensionAndName(String dimensionName, String name) {
        return queryResult();
    }
    
    @Query("SELECT * AS taxonomy FROM taxonomy_by_filter WHERE taxonomy.published = true AND taxonomy.dimensionName = :dimensionName AND taxonomy.name = :name")
    public QueryEffect<TaxonomyRows> getPublishedTaxonomyByDimensionAndName(String dimensionName, String name) {
        return queryResult();
    }

    private NodeRows buildNodeRows(TaxonomyRow taxonomyRow) {
        return new NodeRows(taxonomyRow.rows().stream()
                .map(node -> new NodeRow(node.value(), node.description(), node.aliases(), node.keywords(), node.dimensionSrcHints(), node.parent(), null))
                .toList());
    }

    @Query("SELECT * AS nodeRows FROM taxonomy_by_filter WHERE taxonomy.name = :taxonomyId")
    public QueryEffect<NodeRows> getNodesByTaxonomyId(String taxonomyId) {
        return queryResult();
    }


}
