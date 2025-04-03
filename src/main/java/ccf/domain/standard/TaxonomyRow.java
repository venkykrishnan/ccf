package ccf.domain.standard;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import ccf.domain.standard.Taxonomy.TaxRowAdd;
import ccf.domain.standard.Taxonomy.TaxRowUpdate;
import ccf.domain.standard.Taxonomy.TaxRowsAdd;
import ccf.domain.standard.Taxonomy.TaxRowsRemove;

// TaxonomyRow represents a Taxonomy with its nodes stored in a Nodes record
public record TaxonomyRow(String name, String description, TaxonomyVersion version, String dimension, List<Node> rows,
                Boolean isPublished) {
        public record Node(String id, String value, String description, List<String> aliases, List<String> keywords,
                        List<String> dimensionSrcHints, // columns in src dimension table. key is the dimension name,
                                                       // value is the list of column names.
                        String parent) {
        }

        public record Nodes(List<Node> rows) {
        }

        public record TaxonomyByDimensionAndName(String dimension, String name) {
        }
        // No need for @JsonTypeInfo and @JsonSubTypes here since KeyValue is a simple record
        // with String fields that Jackson can serialize/deserialize automatically
        public record KeyValue(String key, String value) {
        }

        private List<String> convertDimensionSrcHintsToStrings(Map<String, List<String>> dimensionSrcHints) {
                return dimensionSrcHints.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(value -> entry.getKey() + ": " + value))
                        .toList();
        }
        
        private List<KeyValue> flattenDimensionSrcHints(Map<String, List<String>> dimensionSrcHints) {
                return dimensionSrcHints.entrySet().stream()
                        .flatMap(entry -> entry.getValue().stream()
                                .map(value -> new KeyValue(entry.getKey(), value)))
                        .toList();
        }

        public TaxonomyRow onTaxonomyPublished(Boolean isPublished) {
                return new TaxonomyRow(this.name(), this.description(), this.version(), this.dimension(), this.rows(),
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowAdded(TaxRowAdd taxRowAdd) {
                var newRows = new java.util.ArrayList<Node>(this.rows());
                newRows.add(new Node(taxRowAdd.row().rowId(), taxRowAdd.row().value(), taxRowAdd.row().description(), taxRowAdd.row().aliases(), taxRowAdd.row().keywords(),
                                convertDimensionSrcHintsToStrings(taxRowAdd.row().dimensionSrcHints()), taxRowAdd.row().parent()));
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowsAdded(TaxRowsAdd taxRowsAdd) {
                var newRows = new java.util.ArrayList<>(this.rows());
                if (taxRowsAdd.isReplace()) {
                    newRows.clear();
                    taxRowsAdd.rows().stream()
                        .map(row -> new Node(row.rowId(), row.value(), row.description(), row.aliases(), row.keywords(),
                                convertDimensionSrcHintsToStrings(row.dimensionSrcHints()), row.parent()))
                        .forEach(newRows::add);
                } else {
                    taxRowsAdd.rows().stream()
                        .map(row -> new Node(row.rowId(), row.value(), row.description(), row.aliases(), row.keywords(),
                                convertDimensionSrcHintsToStrings(row.dimensionSrcHints()), row.parent()))
                        .forEach(newRows::add);
                }
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowRemoved(String rowId) {
                var newRows = new java.util.ArrayList<>(this.rows());
                newRows.removeIf(row -> row.id().equals(rowId));
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }
        public TaxonomyRow onTaxonomyTaxRowsRemoved(TaxRowsRemove taxRowsRemove) {
                var newRows = new java.util.ArrayList<>(this.rows());
                newRows.removeAll(taxRowsRemove.rowIds().stream()
                        .map(id -> new Node(id, "", "", List.of(), List.of(), List.of(), ""))
                        .toList());
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);   
        }
        
        public TaxonomyRow onTaxonomyTaxRowUpdated(TaxRowUpdate taxRowUpdate) {
                var newRows = new java.util.ArrayList<>(this.rows());
                var row = newRows.stream()
                        .filter(r -> r.id().equals(taxRowUpdate.rowId()))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.name(), "Tax row with ID '" + taxRowUpdate.rowId() + "' not found"));
                newRows.set(newRows.indexOf(row), new Node(row.id(), taxRowUpdate.row().value(), taxRowUpdate.row().description(), taxRowUpdate.row().aliases(), taxRowUpdate.row().keywords(),
                                        convertDimensionSrcHintsToStrings(taxRowUpdate.row().dimensionSrcHints()), taxRowUpdate.row().parent()));
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }
}