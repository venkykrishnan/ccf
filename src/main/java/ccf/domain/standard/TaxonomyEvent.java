package ccf.domain.standard;
import akka.javasdk.annotations.TypeName;
import ccf.domain.standard.Taxonomy.TaxRowAdd;
import ccf.domain.standard.Taxonomy.TaxRowRemove;
import ccf.domain.standard.Taxonomy.TaxRowUpdate;
import ccf.domain.standard.Taxonomy.TaxRowsAdd;
import ccf.domain.standard.Taxonomy.TaxRowsRemove;
import ccf.domain.standard.Taxonomy.TaxonomyCreate;
import ccf.domain.standard.Taxonomy.TaxonomyPublish;
import ccf.domain.standard.Taxonomy.TaxonomyRemove;
public sealed interface TaxonomyEvent {
    @TypeName("taxonomy-created")
    record TaxonomyCreated(TaxonomyCreate taxonomyCreate) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-removed")
    record TaxonomyRemoved() implements TaxonomyEvent {
    }
    @TypeName("taxonomy-published")
    record TaxonomyPublished(TaxonomyPublish taxonomyPublish) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-taxrow-added")
    record TaxonomyTaxRowAdded(TaxRowAdd taxRowAdd) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-taxrow-removed")
    record TaxonomyTaxRowRemoved(TaxRowRemove taxRowRemove) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-taxrows-added")
    record TaxonomyTaxRowsAdded(TaxRowsAdd taxRowsAdd) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-taxrows-removed")
    record TaxonomyTaxRowsRemoved(TaxRowsRemove taxRowsRemove) implements TaxonomyEvent {
    }
    @TypeName("taxonomy-taxrow-updated")
    record TaxonomyTaxRowUpdated(TaxRowUpdate taxRowUpdate) implements TaxonomyEvent {
    }
}