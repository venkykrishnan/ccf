package ccf.domain.standard;
import akka.javasdk.annotations.TypeName;
import ccf.domain.standard.Taxonomy.TaxonomyCreate;

public sealed interface TaxonomyEvent {
    @TypeName("taxonomy-created")
    record TaxonomyCreated(Taxonomy.TaxonomyCreate taxonomyCreate) implements TaxonomyEvent {
    }
}
≥