package ccf.domain.standard;

import java.util.List;
import java.util.Map;

// Standard dimensions are for dimensions with String values (not for period or boolean values).
// A standard dimension can be associated with several domains.
// Standard dimensions are used to define taxonomies for a standard.
// A Taxonomy is named and has a description. It has a default version and a list of versions.
public record StandardDimension(String name, String description,
                                List<String> domains,
                                Map<String, Taxonomy> taxonomies) {

    public record StandardDimensionCreate(String name, String description, List<String> domains) {}
    public record TaxonomyCreate(String dimensionName, String name, String description) {}
    public record TaxonomyVersionCreate(String dimensionName, String taxonomyName, StandardVersion standardVersion, Boolean isDefault) {}
    public record TaxonomyVersionPublish(String dimensionName, String taxonomyName, StandardVersion standardVersion, Boolean isPublished) {}
    public record TaxonomyVersionDefault(String dimensionName, String taxonomyName, StandardVersion standardVersion) {}
    public record DimensionRowAdd(String dimensionName, String taxonomyName, String versionName, StandardDimensionRow row) {}
    public record DimensionRowsAdd(String dimensionName, String taxonomyName, String versionName, List<StandardDimensionRow> rows) {}
    public record DimensionRowRemove(String dimensionName, String taxonomyName, String versionName, String value) {}
    public record DimensionRowsRemove(String dimensionName, String taxonomyName, StandardVersion version) {}
    public record TaxonomyVersionRemove(String dimensionName, String taxonomyName, StandardVersion version) {}
    public record TaxonomyRemove(String dimensionName, String taxonomyName) {}
    public record DimensionRemove(String dimensionName) {}
    public record DomainRemove(String domainName) {}
    // The taxonomyMap is a map of a Taxonomy Name to a Taxonomy object
    public record Taxonomy(String name, String description, StandardVersion defaultVersion,
                           List<TaxonomyVersion> taxonomyVersions) {
    }
    public record TaxonomyVersion(StandardVersion version, Boolean isPublished, List<StandardDimensionRow> rows) {
    }
    
    public record StandardDimensionRow(String value, String description,
            List<String> aliases,
            List<String> keywords,
            Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the dimension name, value is the list of column names.   
            String parent,
            List<String> children) {

    }
}
