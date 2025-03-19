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
    record StandardDimensionRowsAdded(String dimensionName, String taxonomyName, String versionName,
                                      List<StandardDimension.StandardDimensionRow> dimensionRows) implements StandardEvent {
    }
    @TypeName("standard-dimension-row-added")
    record StandardDimensionRowAdded(String dimensionName, String taxonomyName, String versionName,
                                     StandardDimension.StandardDimensionRow dimensionRow) implements StandardEvent {
    }
    @TypeName("standard-dimension-default-version-changed")
    record StandardDimensionDefaultVersionChanged(String dimensionName, String taxonomyName,
                                                  StandardVersion version) implements StandardEvent {
    }
    @TypeName("standard-dimension-row-changed")
    record StandardDimensionRowChanged(String dimensionName, String taxonomyName, String versionName,
                                       StandardDimension.StandardDimensionRow dimensionRow) implements StandardEvent {
    }
    //</editor-fold>
    //<editor-fold desc="Clean up">
    // Cleaning up: should not remove row that's a parent
    @TypeName("standard-dimension-row-removed")
    record StandardDimensionRowRemoved(String dimensionName, String taxonomyName, String versionName,
                                       String value) implements StandardEvent {
    }
    // Cleaning up: initialize dimensions-rows (to List.of()) for this TaxonomyVersion
    // do not allow deleting the TaxonomyVersion if its the defaultVersion
    @TypeName("standard-dimension-rows-removed")
    record StandardDimensionRowsRemoved(String dimensionName, String taxonomyName,
                                        StandardVersion version) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-version-removed")
    record StandardTaxonomyVersionRemoved(String dimensionName, String taxonomyName,
                                                   StandardVersion version) implements StandardEvent {
    }
    @TypeName("standard-taxonomy-removed")
    record StandardTaxonomyRemoved(String dimensionName, String taxonomyName) implements StandardEvent {
    }
    @TypeName("standard-dimension-removed")
    record StandardDimensionRemoved(String dimensionName) implements StandardEvent {
    }
    @TypeName("standard-domain-removed")
    record StandardDomainRemoved(String domainName) implements StandardEvent {
    }
    //</editor-fold>
}
