package ccf.domain.standard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccf.util.CCFLog;

public record Taxonomy(String id, String name, String description, TaxonomyVersion version,
        TaxonomyStatus status, List<Row> rows) {

    private static final Logger logger = LoggerFactory.getLogger(Taxonomy.class);

    public record TaxonomyCreate(String name, String description, TaxonomyVersion version) {
    }

    public record TaxRowAdd(TaxRow taxRow) {
    }

    public record TaxRowsAdd(List<TaxRow> taxRows, Boolean isReplace) {
    }

    public record TaxRowsRemove(List<String> rowIds) {
    }

    public record TaxRowUpdate(String rowId, Updates row) {
        public record Updates(String value, String description, List<String> aliases, List<String> keywords,
                Map<String, List<String>> dimensionSrcHints, String parent) {
        }
    }

    public record Row(String rowId, String value, String description, List<String> aliases, List<String> keywords,
            Map<String, List<String>> dimensionSrcHints, String parent, List<String> children, String formula) {
    }
    
    public record TaxRow(String rowId, String value, String description, List<String> aliases, List<String> keywords,
            Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the dimension name,
                                                         // value is the list of column names.
            String parent) {

        public TaxRow(String value, String description, List<String> aliases, List<String> keywords,
                Map<String, List<String>> dimensionSrcHints, String parent) {
            this(java.util.UUID.randomUUID().toString(), value, description, aliases, keywords, dimensionSrcHints,
                    parent);
        }
    }
    
    public Taxonomy onTaxonomyCreated(TaxonomyEvent.TaxonomyCreated created) {
        CCFLog.info(logger, "Taxonomy created", Map.of("taxonomy", created.taxonomyCreate().toString()));

        return new Taxonomy(this.id, created.taxonomyCreate().name(), created.taxonomyCreate().description(),
                created.taxonomyCreate().version(), TaxonomyStatus.TAXONOMY_INITIALIZED, List.<Row>of());
    }

    public Taxonomy onTaxonomyRemoved(TaxonomyEvent.TaxonomyRemoved removed) {
        CCFLog.info(logger, "Taxonomy removed", Map.of("taxonomy", this.id));
        return new Taxonomy(this.id(), null, null, null, TaxonomyStatus.TAXONOMY_EMPTY, List.of());
    }

    public Taxonomy onTaxonomyPublished(TaxonomyEvent.TaxonomyPublished published) {
        CCFLog.info(logger, "Taxonomy published",
                Map.of("taxonomy", this.id, "isPublished", published.isPublish().toString()));
        if (!published.isPublish()) {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(),
                    TaxonomyStatus.TAXONOMY_INITIALIZED, this.rows());
        } else {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(),
                    TaxonomyStatus.TAXONOMY_PUBLISHED, this.rows());
        }
    }

    private void addRowAndUpdateParent(TaxRow taxRow) {
        // Convert TaxRow to Row
        Row newRow = new Row(
            taxRow.rowId(),
            taxRow.value(), 
            taxRow.description(),
            taxRow.aliases(),
            taxRow.keywords(),
            taxRow.dimensionSrcHints(),
            taxRow.parent(),
            new ArrayList<>(),
            null
        );

        // Get parent row if it exists
        if (taxRow.parent() != null) {
            Row parentRow = rows.stream()
                .filter(r -> r.rowId().equals(taxRow.parent()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.id(), "Parent row with ID '" + taxRow.parent() + "' not found"));

            // Create new parent row with updated children
            List<String> updatedChildren = new ArrayList<>(parentRow.children());
            updatedChildren.add(newRow.rowId());
            
            Row updatedParent = new Row(
                parentRow.rowId(),
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
            rows.remove(parentRow);
            rows.add(updatedParent);
        }

        // Add new row
        rows.add(newRow);
    }

    public Taxonomy onTaxonomyTaxRowAdded(TaxonomyEvent.TaxonomyTaxRowAdded added) {
        CCFLog.info(logger, "Taxonomy tax row added", Map.of("taxonomy", added.taxRowAdd().toString()));
        addRowAndUpdateParent(added.taxRowAdd().taxRow());
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.status(),
                rows);
    }
    private void removeRowAndUpdateParent(String rowId) {
        // Check if row is a parent of any other rows
        boolean isParent = rows.stream()
            .anyMatch(r -> rowId.equals(r.parent()));
        
        if (isParent) {
            throw new TaxonomyException(this.id(), "Cannot remove row with ID '" + rowId + "' because it is a parent of other rows");
        }
        // Get the row to be removed
        Row taxRow = rows.stream()
            .filter(r -> r.rowId().equals(rowId))
            .findFirst()
            .orElseThrow(() -> new TaxonomyException(this.id(), "Tax row with ID '" + rowId + "' not found"));

        // If row has a parent, remove it from parent's children list
        if (taxRow.parent() != null) {
            Row parentRow = rows.stream()
                .filter(r -> r.rowId().equals(taxRow.parent()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.id(), "Parent row with ID '" + taxRow.parent() + "' not found"));

            // Create new parent row with updated children
            List<String> updatedChildren = new ArrayList<>(parentRow.children());
            updatedChildren.remove(taxRow.rowId());
            
            Row updatedParent = new Row(
                parentRow.rowId(),
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
            rows.remove(parentRow);
            rows.add(updatedParent);
        }

        // Remove the row by matching rowId
        rows.removeIf(row -> row.rowId().equals(taxRow.rowId()));
    }

    public Taxonomy onTaxonomyTaxRowRemoved(TaxonomyEvent.TaxonomyTaxRowRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax row removed", Map.of("taxonomy", removed.rowId()));
        removeRowAndUpdateParent(removed.rowId());
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.status(), this.rows());
    }
       
    public Taxonomy onTaxonomyTaxRowsAdded(TaxonomyEvent.TaxonomyTaxRowsAdded added) {
        CCFLog.info(logger, "Taxonomy tax rows added", Map.of("taxonomy", added.taxRowsAdd().toString()));
        var rows = this.rows();
        if (added.taxRowsAdd().isReplace()) {
            rows.clear();
        }
        added.taxRowsAdd().taxRows().forEach(this::addRowAndUpdateParent);
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.status(), rows);
    }

    public Taxonomy onTaxonomyTaxRowsRemoved(TaxonomyEvent.TaxonomyTaxRowsRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax rows removed", Map.of("taxonomy", removed.taxRowsRemove().toString()));
        removed.taxRowsRemove().rowIds().forEach(this::removeRowAndUpdateParent);
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.status(),
                rows);
    }
    private void updateRowAndUpdateParent(TaxRowUpdate taxRowUpdate) {
        // Find the existing row
        Row existingRow = rows.stream()
                .filter(r -> r.rowId().equals(taxRowUpdate.rowId()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.id(),
                        "Tax row with ID '" + taxRowUpdate.rowId() + "' not found"));

        // Handle parent change if needed
        if (!existingRow.parent().equals(taxRowUpdate.row().parent())) {
            // Remove from old parent's children if it had a parent
            if (existingRow.parent() != null) {
                Row oldParentRow = rows.stream()
                        .filter(r -> r.rowId().equals(existingRow.parent()))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.id(),
                                "Parent row with ID '" + existingRow.parent() + "' not found"));

                List<String> updatedOldParentChildren = new ArrayList<>(oldParentRow.children());
                updatedOldParentChildren.remove(existingRow.rowId());

                Row updatedOldParent = new Row(
                    oldParentRow.rowId(),
                    oldParentRow.value(),
                    oldParentRow.description(),
                    oldParentRow.aliases(),
                    oldParentRow.keywords(),
                    oldParentRow.dimensionSrcHints(),
                    oldParentRow.parent(),
                    updatedOldParentChildren,
                    oldParentRow.formula()
                );

                rows.remove(oldParentRow);
                rows.add(updatedOldParent);
            }

            // Add to new parent's children if it has a new parent
            if (taxRowUpdate.row().parent() != null) {
                Row newParentRow = rows.stream()
                        .filter(r -> r.rowId().equals(taxRowUpdate.row().parent()))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.id(),
                                "New parent row with ID '" + taxRowUpdate.row().parent() + "' not found"));

                List<String> updatedNewParentChildren = new ArrayList<>(newParentRow.children());
                updatedNewParentChildren.add(existingRow.rowId());

                Row updatedNewParent = new Row(
                    newParentRow.rowId(),
                    newParentRow.value(),
                    newParentRow.description(),
                    newParentRow.aliases(),
                    newParentRow.keywords(),
                    newParentRow.dimensionSrcHints(),
                    newParentRow.parent(),
                    updatedNewParentChildren,
                    newParentRow.formula()
                );

                rows.remove(newParentRow);
                rows.add(updatedNewParent);
            }
        }

        // Update the row itself
        Row updatedRow = new Row(
            existingRow.rowId(),
            taxRowUpdate.row().value(),
            taxRowUpdate.row().description(),
            taxRowUpdate.row().aliases(),
            taxRowUpdate.row().keywords(),
            taxRowUpdate.row().dimensionSrcHints(),
            taxRowUpdate.row().parent(),
            existingRow.children(),
            existingRow.formula()
        );

        rows.remove(existingRow);
        rows.add(updatedRow);
    }

    public Taxonomy onTaxonomyTaxRowUpdated(TaxonomyEvent.TaxonomyTaxRowUpdated updated) {
        CCFLog.info(logger, "Taxonomy tax row updated", Map.of("taxonomy", updated.taxRowUpdate().toString()));
        updateRowAndUpdateParent(updated.taxRowUpdate());
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.status(), this.rows());
    }
}
