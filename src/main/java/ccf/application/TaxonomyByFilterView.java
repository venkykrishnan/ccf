package ccf.application;

import java.util.List;

import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.Consume;
import akka.javasdk.annotations.Query;
import akka.javasdk.view.TableUpdater;
import akka.javasdk.view.View;
import ccf.domain.standard.StandardDimension.StandardDimensionRow;
import ccf.domain.standard.StandardEvent;
import ccf.domain.standard.TaxonomyRow;
import ccf.domain.standard.Taxonomies;

@ComponentId("taxonomy_by_filter")
public class TaxonomyByFilterView extends View {

    @Consume.FromEventSourcedEntity(StandardEntity.class)
    public static class TaxonomyByFilter extends TableUpdater<TaxonomyRow> { // <2>
        public Effect<TaxonomyRow> onEvent(StandardEvent event) { // <3>
            var ret = switch (event) { // Need to add all the cases for the StandardEvent
                case StandardEvent.StandardCreated created ->
                    effects().ignore();
                case StandardEvent.StandardDomainAdded domainAdded ->
                    effects().ignore();
                case StandardEvent.StandardDimensionAdded dimensionAdded ->    
                    effects().ignore();
                case StandardEvent.StandardTaxonomyAdded added ->
                effects().ignore();
                case StandardEvent.StandardTaxonomyVersionAdded versionAdded ->
                effects().updateRow(new TaxonomyRow(versionAdded.taxonomyVersionCreate().taxonomyName(),
                versionAdded.taxonomyVersionCreate().description(),
                versionAdded.taxonomyVersionCreate().standardVersion(),
                List.<StandardDimensionRow>of(),
                false, false, versionAdded.taxonomyVersionCreate().dimensionName()
                ));
            case StandardEvent.StandardTaxonomyPublish versionPublished ->
                
                    
                case StandardEvent.StandardTaxonomyDefaultVersionSet versionDefaultSet ->
                    effects().ignore();
                case StandardEvent.StandardDimensionRowAdded rowAdded ->
                    effects().ignore();
                case StandardEvent.StandardDimensionRowsAdded rowsAdded ->
                    effects().ignore();
                case StandardEvent.StandardDimensionRowRemoved rowRemoved ->
                    effects().ignore();
                case StandardEvent.StandardDimensionRowsRemoved rowsRemoved ->
                    effects().ignore();
                case StandardEvent.StandardTaxonomyVersionRemoved versionRemoved ->
                    effects().ignore();
                case StandardEvent.StandardTaxonomyRemoved taxonomyRemoved ->
                    effects().ignore();
                case StandardEvent.StandardDimensionRemoved dimensionRemoved ->
                    effects().ignore();
                case StandardEvent.StandardDomainRemoved domainRemoved ->
                    effects().ignore();
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
