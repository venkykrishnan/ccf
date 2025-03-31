package ccf.domain.standard;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccf.util.CCFLog;

public record Taxonomy(String id, String name, String description, TaxonomyVersion version, String dimension,
        TaxonomyStatus status, List<TaxRow> rows) {

    private static final Logger logger = LoggerFactory.getLogger(Taxonomy.class);

    public record TaxonomyCreate(String dimensionName, String name, String description, TaxonomyVersion version) {
    }

    public record TaxRowAdd(TaxRow row) {
    }

    public record TaxRowsAdd(List<TaxRow> rows, Boolean isReplace) {
    }

    public record TaxRowsRemove(List<String> rowIds) {
    }

    public record TaxRowUpdate(String rowId, Updates row) {
        public record Updates(String value, String description, List<String> aliases, List<String> keywords,
                Map<String, List<String>> dimensionSrcHints, String parent) {
        }
    }

    public record TaxRowWithChildren(String rowId, String value, String description, List<String> aliases, List<String> keywords,
            Map<String, List<String>> dimensionSrcHints, String parent, List<String> children) {

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

        if (this.status() != TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id, "Taxonomy already exists");
        }

        return new Taxonomy(this.id, created.taxonomyCreate().name(), created.taxonomyCreate().description(),
                created.taxonomyCreate().version(), created.taxonomyCreate().dimensionName(),
                TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
    }

    public Taxonomy onTaxonomyRemoved(TaxonomyEvent.TaxonomyRemoved removed) {
        CCFLog.info(logger, "Taxonomy removed", Map.of("taxonomy", this.id));
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED || this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot remove - taxonomy is disabled or published");
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(),
                TaxonomyStatus.TAXONOMY_DISABLED, List.<TaxRow>of());
    }

    public Taxonomy onTaxonomyPublished(TaxonomyEvent.TaxonomyPublished published) {
        CCFLog.info(logger, "Taxonomy published",
                Map.of("taxonomy", this.id, "isPublished", published.isPublish().toString()));
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot publish - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED && published.isPublish()) {
            throw new TaxonomyException(this.id(), "Taxonomy already published");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED && !published.isPublish()) {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(),
                    TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(),
                TaxonomyStatus.TAXONOMY_PUBLISHED, this.rows());
    }

    public Taxonomy onTaxonomyTaxRowAdded(TaxonomyEvent.TaxonomyTaxRowAdded added) {
        CCFLog.info(logger, "Taxonomy tax row added", Map.of("taxonomy", added.taxRowAdd().toString()));
        var rows = this.rows();
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot add tax row - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot add tax row - taxonomy is published");
        }
        if (rows.stream().anyMatch(row -> row.rowId().equals(added.taxRowAdd().row().rowId()))) {
            throw new TaxonomyException(this.id(),
                    "Tax row with ID '" + added.taxRowAdd().row().rowId() + "' already exists");
        }
        if (rows.stream().anyMatch(row -> row.value().equals(added.taxRowAdd().row().value()))) {
            throw new TaxonomyException(this.id(),
                    "Tax row with value '" + added.taxRowAdd().row().value() + "' already exists");
        }
        validForAddUpdate(added.taxRowAdd().row().rowId(), added.taxRowAdd().row().value(),
                added.taxRowAdd().row().parent(), true);
        rows.add(added.taxRowAdd().row());

        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                rows);
    }

    private void validForAddUpdate(String rowId, String value, String parent, Boolean checkDuplicates) {
        if (checkDuplicates) {
            if (rows.stream().anyMatch(row -> row.rowId().equals(rowId))) {
                throw new TaxonomyException(this.id(), "Tax row with ID '" + rowId + "' already exists");
            }
            if (rows.stream().anyMatch(row -> row.value().equals(value))) {
                throw new TaxonomyException(this.id(), "Tax row with value '" + value + "' already exists");
            }
        }
        if (parent != null &&
                rows.stream().noneMatch(r -> r.rowId().equals(parent))) {
            throw new TaxonomyException(this.id(), "Parent row with ID '" + parent + "' does not exist");
        }
    }

    public Taxonomy onTaxonomyTaxRowsAdded(TaxonomyEvent.TaxonomyTaxRowsAdded added) {
        CCFLog.info(logger, "Taxonomy tax rows added", Map.of("taxonomy", added.taxRowsAdd().toString()));
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot add tax rows - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot add tax rows - taxonomy is published");
        }
        var rows = this.rows();
        if (added.taxRowsAdd().isReplace()) {
            rows.clear();
            rows.addAll(added.taxRowsAdd().rows());
        } else {
            for (TaxRow newRow : added.taxRowsAdd().rows()) {
                if (rows.stream().anyMatch(row -> row.rowId().equals(newRow.rowId()))) {
                    throw new TaxonomyException(this.id(), "Tax row with ID '" + newRow.rowId() + "' already exists");
                }
                if (rows.stream().anyMatch(row -> row.value().equals(newRow.value()))) {
                    throw new TaxonomyException(this.id(),
                            "Tax row with value '" + newRow.value() + "' already exists");
                }
                validForAddUpdate(newRow.rowId(), newRow.value(), newRow.parent(), true);
            }
            rows.addAll(added.taxRowsAdd().rows());
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                rows);
    }

    private List<TaxRow> removeTaxonomyRows(List<String> rowId) {
        List<TaxRow> rows = this.rows();

        List<TaxRow> rowsToRemove = rowId.stream()
                .map(id -> rows.stream()
                        .filter(r -> r.rowId().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new TaxonomyException(this.id(), "Tax row with ID '" + id + "' not found")))
                .toList();

        if (rows.stream().anyMatch(
                r -> r.parent() != null && rowsToRemove.stream().anyMatch(t -> t.rowId().equals(r.parent())))) {
            throw new TaxonomyException(this.id(), "Cannot remove tax row - it is a parent of other rows");
        }
        rows.removeAll(rowsToRemove);
        return rows;
    }

    private void validateTaxonomyStatusForRemove(String action) {
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot " + action + " - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot " + action + " - taxonomy is published");
        }
    }

    public Taxonomy onTaxonomyTaxRowRemoved(TaxonomyEvent.TaxonomyTaxRowRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax row removed", Map.of("taxonomy", removed.rowId()));
        validateTaxonomyStatusForRemove("remove tax row");
        List<TaxRow> newRows = removeTaxonomyRows(List.of(removed.rowId()));
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                newRows);
    }

    public Taxonomy onTaxonomyTaxRowsRemoved(TaxonomyEvent.TaxonomyTaxRowsRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax rows removed", Map.of("taxonomy", removed.taxRowsRemove().toString()));
        validateTaxonomyStatusForRemove("remove tax rows");
        List<TaxRow> newRows = removeTaxonomyRows(removed.taxRowsRemove().rowIds());
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                newRows);
    }

    public Taxonomy onTaxonomyTaxRowUpdated(TaxonomyEvent.TaxonomyTaxRowUpdated updated) {
        CCFLog.info(logger, "Taxonomy tax row updated", Map.of("taxonomy", updated.taxRowUpdate().toString()));
        var rows = this.rows();
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot update tax row - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot update tax row - taxonomy is published");
        }
        validForAddUpdate(updated.taxRowUpdate().rowId(), updated.taxRowUpdate().row.value(),
                updated.taxRowUpdate().row.parent(), false);
        TaxRow row = rows.stream()
                .filter(r -> r.rowId().equals(updated.taxRowUpdate().rowId()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.id(),
                        "Tax row with ID '" + updated.taxRowUpdate().rowId() + "' not found"));

        rows.set(rows.indexOf(row), new TaxRow(row.rowId(), row.value(), updated.taxRowUpdate().row.description(),
                updated.taxRowUpdate().row.aliases(), updated.taxRowUpdate().row.keywords(),
                updated.taxRowUpdate().row.dimensionSrcHints(), updated.taxRowUpdate().row.parent()));

        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                rows);
    }

}
