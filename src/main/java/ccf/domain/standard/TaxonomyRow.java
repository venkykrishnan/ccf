package ccf.domain.standard;

import java.util.List;
import java.util.Map;

import ccf.domain.standard.Taxonomy.TaxRowAdd;
import ccf.domain.standard.Taxonomy.TaxRowUpdate;
import ccf.domain.standard.Taxonomy.TaxRowsAdd;
import ccf.domain.standard.Taxonomy.TaxRowsRemove;

// TaxonomyRow is a combination of a Taxonomy and a TaxonomyVersion
public record TaxonomyRow(String name, String description, TaxonomyVersion version, String dimension, List<Nodes> rows,
                Boolean isPublished) {

        public record Nodes(String id, String value, String description, List<String> aliases, List<String> keywords,
                        Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the
                                                                     // dimension name,
                                                                     // value is the list of column names.
                        String parent) {
        }

        public TaxonomyRow onTaxonomyPublished(Boolean isPublished) {
                return new TaxonomyRow(this.name(), this.description(), this.version(), this.dimension(), this.rows(),
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowAdded(TaxRowAdd taxRowAdd) {
                var newRows = new java.util.ArrayList<>(this.rows());
                newRows.add(new Nodes(taxRowAdd.row().rowId(), taxRowAdd.row().value(), taxRowAdd.row().description(), taxRowAdd.row().aliases(), taxRowAdd.row().keywords(),
                                taxRowAdd.row().dimensionSrcHints(), taxRowAdd.row().parent()));
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowsAdded(TaxRowsAdd taxRowsAdd) {
                var newRows = new java.util.ArrayList<>(this.rows());
                if (taxRowsAdd.isReplace()) {
                    newRows.clear();
                    taxRowsAdd.rows().stream()
                        .map(row -> new Nodes(row.rowId(), row.value(), row.description(), row.aliases(), row.keywords(),
                                row.dimensionSrcHints(), row.parent()))
                        .forEach(newRows::add);
                } else {
                    taxRowsAdd.rows().stream()
                        .map(row -> new Nodes(row.rowId(), row.value(), row.description(), row.aliases(), row.keywords(),
                                row.dimensionSrcHints(), row.parent()))
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
                        .map(id -> new Nodes(id, "", "", List.of(), List.of(), Map.of(), ""))
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
                newRows.set(newRows.indexOf(row), new Nodes(row.id(), taxRowUpdate.row().value(), taxRowUpdate.row().description(), taxRowUpdate.row().aliases(), taxRowUpdate.row().keywords(),
                                taxRowUpdate.row().dimensionSrcHints(), taxRowUpdate.row().parent()));
                return new TaxonomyRow(name, description, version, dimension, newRows,
                                isPublished);
        }
}