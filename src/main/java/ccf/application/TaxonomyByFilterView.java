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
            var ret = switch (event) { // Need to add all the cases for the StandardEvent
                case StandardEvent.StandardCreated created ->
                    effects().ignore();
                case StandardEvent.StandardDomainAdded domainAdded ->
                    effects().ignore();
                // HIA: 25 Mar 2025
                case StandardEvent.StandardDimensionAdded dimensionAdded ->    
                    effects().ignore();
                case StandardEvent.StandardTaxonomyAdded added ->
                    effects().updateRow(new TaxonomyRow(added.taxonomyCreate().name(),
                    added.taxonomyCreate().description(),
                    added.taxonomyCreate().defaultVersion(),
                    added.taxonomyCreate().taxonomyVersions()));
                case StandardEvent.StandardTaxonomyVersionAdded versionAdded ->
                    effects().ignore();
                case StandardEvent.StandardTaxonomyPublish versionPublished ->
                    effects().ignore();
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
