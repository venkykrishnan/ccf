package ccf.domain.standard;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public record Taxonomy(String id, String name, String description, Boolean isPublished, StandardVersion version, String dimension, List<TaxRow> rows) {
    private static final Logger logger = LoggerFactory.getLogger(Standard.class);

    public record TaxonomyCreate(String dimensionName, String name, String description, StandardVersion version) {}
    public record TaxonomyRemove(String id) {}
    public record TaxonomyPublish(String id, Boolean isPublish) {}
    public record TaxRowAdd(String id, TaxRow row) {}
    public record TaxRowRemove(String id, String rowId) {}
    public record TaxRowUpdate(String id, String rowId, TaxRow row) {}
    public record TaxRowsAdd(String id, List<TaxRow> rows, Boolean isReplace) {}
    public record TaxRowsRemove(String id, List<String> rowIds) {}
    public record TaxRow(String rowId, String value, String description, List<String> aliases, List<String> keywords, Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the dimension name, value is the list of column names.   
        String parent,
        List<String> children) {}
}
