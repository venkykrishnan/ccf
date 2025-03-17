package ccf.domain.standard;

import java.util.List;
import java.util.Map;

// Standard dimensions are for dimensions with String values (not for period or boolean values).
// A standard dimension can be associated with several domains.
// Standard dimensions are used to define taxonomies for a standard.
// A Taxonomy is named and has a description. It has a default version and a list of versions.
public record StandardDimension(String name, String description,
                                List<String> Domain,
                                Map<String, Taxonomy> taxonomies) {
    // The taxonomyMap is a map of a Taxonomy Name to a Taxonomy object
    public record Taxonomy(String name, String description, StandardVersion defaultVersion,
                           List<TaxonomyVersion> taxonomyVersions) {
    }
    public record TaxonomyVersion(StandardVersion version, List<StandardDimensionRow> rows) {

    }
    public record StandardDimensionRow(String value, String parent) {
    }
}
