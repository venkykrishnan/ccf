package ccf.domain.standard;

import akka.javasdk.annotations.TypeName;

import java.util.List;

public sealed interface StandardEvent {
    //<editor-fold desc="Create & Modify">
    @TypeName("standard-created")
    record StandardCreated(Standard.StandardCreate standardCreate) implements StandardEvent {
    }
    @TypeName("standard-domain-added")
    record StandardDomainAdded(StandardDomain standardDomain) implements StandardEvent {
    }
    @TypeName("standard-dimension-added")
    record StandardDimensionAdded(StandardDimension.StandardDimensionCreate standardDimensionCreate) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-added")
    record StandardTaxonomyAdded(StandardDimension.TaxonomyCreate taxonomyCreate) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-version-added")
    record StandardTaxonomyVersionAdded(StandardDimension.TaxonomyVersionCreate taxonomyVersionCreate) implements StandardEvent {
    }
    @TypeName("standard-dimension-rows-added")
    record StandardDimensionRowsAdded(StandardDimension.DimensionRowsAdd rowsAdd) implements StandardEvent {
    }
    @TypeName("standard-dimension-row-added")
    record StandardDimensionRowAdded(StandardDimension.DimensionRowAdd rowAdd) implements StandardEvent {
    }
    @TypeName("standard-dimension-default-version-changed")
    record StandardTaxonomyPublish(StandardDimension.TaxonomyVersionPublish taxonomyVersionPublish) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-default-version-set")
    record StandardTaxonomyDefaultVersionSet(StandardDimension.TaxonomyVersionDefault taxonomyVersionDefault) implements StandardEvent {
    }
    //</editor-fold>
    //<editor-fold desc="Clean up">
    // Cleaning up: should not remove row that's a parent
    @TypeName("standard-dimension-row-removed")
    record StandardDimensionRowRemoved(StandardDimension.DimensionRowRemove dimensionRowRemove) implements StandardEvent {
    }
    // Cleaning up: initialize dimensions-rows (to List.of()) for this TaxonomyVersion
    // do not allow deleting the TaxonomyVersion if its the defaultVersion
    @TypeName("standard-dimension-rows-removed")
    record StandardDimensionRowsRemoved(StandardDimension.DimensionRowsRemove dimensionRowsRemove) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-version-removed")
    record StandardTaxonomyVersionRemoved(StandardDimension.TaxonomyVersionRemove taxonomyVersionRemove) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-removed")
    record StandardTaxonomyRemoved(StandardDimension.TaxonomyRemove taxonomyRemove) implements StandardEvent {
    }
    @TypeName("standard-dimension-removed")
    record StandardDimensionRemoved(StandardDimension.DimensionRemove dimensionRemove) implements StandardEvent {
    }
    @TypeName("standard-domain-removed")
    record StandardDomainRemoved(StandardDimension.DomainRemove domainRemove) implements StandardEvent {
    }
    //</editor-fold>
}
