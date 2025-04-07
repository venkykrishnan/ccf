package ccf.domain.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import ccf.domain.standard.Taxonomy.TaxRowAdd;
import ccf.domain.standard.Taxonomy.TaxRowUpdate;
import ccf.domain.standard.Taxonomy.TaxRowsAdd;
import ccf.domain.standard.Taxonomy.TaxRowsRemove;

// TaxonomyRow represents a Taxonomy with its nodes stored in a Nodes record
public record TaxonomyRow(String name, String description, TaxonomyVersion version, String dimension, List<TRRow> trRows,
                Boolean isPublished) {
        public record TRRow(String id, String value, String description, List<String> aliases, List<String> keywords,
                        List<String> dimensionSrcHints, // columns in src dimension table. key is the dimension name,                               // value is the list of column names.
                        String parent, List<String> children, String formula) {
        }

        public record TRRows(List<TRRow> trRows) {
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

        private void addRowAndUpdateParent(Taxonomy.TaxRow taxRow) {
            // Create new row with empty children list
            TRRow newRow = new TRRow(
                taxRow.rowId(),
                taxRow.value(),
                taxRow.description(),
                taxRow.aliases(),
                taxRow.keywords(),
                convertDimensionSrcHintsToStrings(taxRow.dimensionSrcHints()),
                taxRow.parent(),
                new ArrayList<>(),
                null
            );

            // Get parent row if it exists
            if (taxRow.parent() != null) {
                TRRow parentRow = trRows.stream()
                    .filter(r -> r.id().equals(taxRow.parent()))
                    .findFirst()
                    .orElseThrow(() -> new TaxonomyException(this.name, "Parent row with ID '" + taxRow.parent() + "' not found"));

                // Create new parent row with updated children
                List<String> updatedChildren = new ArrayList<>(parentRow.children());
                updatedChildren.add(newRow.id());
                
                TRRow updatedParent = new TRRow(
                    parentRow.id(),
                    parentRow.value(),
                    parentRow.description(),
                    parentRow.aliases(),
                    parentRow.keywords(),
                    parentRow.dimensionSrcHints(),
                    parentRow.parent(),
                    updatedChildren,
                    parentRow.formula()
                );

                // Update parent in rows list
                trRows.remove(parentRow);
                trRows.add(updatedParent);
            }

            // Add new row
            trRows.add(newRow);
        }

        private void removeRowAndUpdateParent(String rowId) {
            // Check if row is a parent of any other rows
            boolean isParent = trRows.stream()
                .anyMatch(r -> rowId.equals(r.parent()));
            
            if (isParent) {
                throw new TaxonomyException(this.name, "Cannot remove row with ID '" + rowId + "' because it is a parent of other rows");
            }

            // Get the row to be removed
            TRRow taxRow = trRows.stream()
                .filter(r -> r.id().equals(rowId))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.name, "Tax row with ID '" + rowId + "' not found"));

            // If row has a parent, remove it from parent's children list
            if (taxRow.parent() != null) {
                TRRow parentRow = trRows.stream()
                    .filter(r -> r.id().equals(taxRow.parent()))
                    .findFirst()
                    .orElseThrow(() -> new TaxonomyException(this.name, "Parent row with ID '" + taxRow.parent() + "' not found"));

                // Create new parent row with updated children
                List<String> updatedChildren = new ArrayList<>(parentRow.children());
                updatedChildren.remove(taxRow.id());
                
                TRRow updatedParent = new TRRow(
                    parentRow.id(),
                    parentRow.value(),
                    parentRow.description(),
                    parentRow.aliases(),
                    parentRow.keywords(),
                    parentRow.dimensionSrcHints(),
                    parentRow.parent(),
                    updatedChildren,
                    parentRow.formula()
                );

                // Update parent in rows list
                trRows.remove(parentRow);
                trRows.add(updatedParent);
            }

            // Remove the row
            trRows.removeIf(row -> row.id().equals(taxRow.id()));
        }

        private void updateRowAndUpdateParent(TaxRowUpdate taxRowUpdate) {
            // Find the existing row
            TRRow existingRow = trRows.stream()
                .filter(r -> r.id().equals(taxRowUpdate.rowId()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.name,
                        "Tax row with ID '" + taxRowUpdate.rowId() + "' not found"));

            // Handle parent change if needed
            if (!existingRow.parent().equals(taxRowUpdate.row().parent())) {
                // Remove from old parent's children if it had a parent
                if (existingRow.parent() != null) {
                    TRRow oldParentRow = trRows.stream()
                        .filter(r -> r.id().equals(existingRow.parent()))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.name,
                                "Parent row with ID '" + existingRow.parent() + "' not found"));

                    List<String> updatedOldParentChildren = new ArrayList<>(oldParentRow.children());
                    updatedOldParentChildren.remove(existingRow.id());

                    TRRow updatedOldParent = new TRRow(
                        oldParentRow.id(),
                        oldParentRow.value(),
                        oldParentRow.description(),
                        oldParentRow.aliases(),
                        oldParentRow.keywords(),
                        oldParentRow.dimensionSrcHints(),
                        oldParentRow.parent(),
                        updatedOldParentChildren,
                        oldParentRow.formula()
                    );

                    trRows.remove(oldParentRow);
                    trRows.add(updatedOldParent);
                }

                // Add to new parent's children if it has a new parent
                if (taxRowUpdate.row().parent() != null) {
                    TRRow newParentRow = trRows.stream()
                        .filter(r -> r.id().equals(taxRowUpdate.row().parent()))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.name,
                                "New parent row with ID '" + taxRowUpdate.row().parent() + "' not found"));

                    List<String> updatedNewParentChildren = new ArrayList<>(newParentRow.children());
                    updatedNewParentChildren.add(taxRowUpdate.rowId());

                    TRRow updatedNewParent = new TRRow(
                        newParentRow.id(),
                        newParentRow.value(),
                        newParentRow.description(),
                        newParentRow.aliases(),
                        newParentRow.keywords(),
                        newParentRow.dimensionSrcHints(),
                        newParentRow.parent(),
                        updatedNewParentChildren,
                        newParentRow.formula()
                    );

                    trRows.remove(newParentRow);
                    trRows.add(updatedNewParent);
                }
            }

            // Update the row itself
            TRRow updatedRow = new TRRow(
                existingRow.id(),
                taxRowUpdate.row().value(),
                taxRowUpdate.row().description(),
                taxRowUpdate.row().aliases(),
                taxRowUpdate.row().keywords(),  
                convertDimensionSrcHintsToStrings(taxRowUpdate.row().dimensionSrcHints()),
                taxRowUpdate.row().parent(),
                existingRow.children(),
                existingRow.formula()
            );
            
            trRows.remove(existingRow);
            trRows.add(updatedRow);
        }

        public TaxonomyRow onTaxonomyPublished(Boolean isPublished) {
                return new TaxonomyRow(this.name, this.description, this.version, this.dimension, this.trRows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowAdded(TaxRowAdd taxRowAdd) {
                addRowAndUpdateParent(taxRowAdd.taxRow());
                return new TaxonomyRow(name, description, version, dimension, trRows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowsAdded(TaxRowsAdd taxRowsAdd) {
                var rows = this.trRows();
                if (taxRowsAdd.isReplace()) {
                    rows.clear();
                }
                taxRowsAdd.taxRows().forEach(this::addRowAndUpdateParent);
                return new TaxonomyRow(name, description, version, dimension, rows,
                                isPublished);
        }

        public TaxonomyRow onTaxonomyTaxRowRemoved(String rowId) {
                removeRowAndUpdateParent(rowId);
                return new TaxonomyRow(name, description, version, dimension, trRows,
                                isPublished);
        }
        public TaxonomyRow onTaxonomyTaxRowsRemoved(TaxRowsRemove taxRowsRemove) {
                var rows = this.trRows();
                taxRowsRemove.rowIds().forEach(this::removeRowAndUpdateParent);
                return new TaxonomyRow(name, description, version, dimension, rows,
                                isPublished);   
        }
        
        public TaxonomyRow onTaxonomyTaxRowUpdated(TaxRowUpdate taxRowUpdate) {
                updateRowAndUpdateParent(taxRowUpdate);
                return new TaxonomyRow(name, description, version, dimension, trRows,
                                isPublished);
        }
}