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

        return new Taxonomy(this.id, created.taxonomyCreate().name(), created.taxonomyCreate().description(),
                created.taxonomyCreate().version(), created.taxonomyCreate().dimensionName(),
                TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
    }

    public Taxonomy onTaxonomyRemoved(TaxonomyEvent.TaxonomyRemoved removed) {
        CCFLog.info(logger, "Taxonomy removed", Map.of("taxonomy", this.id));
        return new Taxonomy(this.id(), null, null, null, null, TaxonomyStatus.TAXONOMY_EMPTY, List.of());
    }

    public Taxonomy onTaxonomyPublished(TaxonomyEvent.TaxonomyPublished published) {
        CCFLog.info(logger, "Taxonomy published",
                Map.of("taxonomy", this.id, "isPublished", published.isPublish().toString()));
        if (!published.isPublish()) {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(),
                    TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
        } else {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(),
                    TaxonomyStatus.TAXONOMY_PUBLISHED, this.rows());
        }
    }

    public Taxonomy onTaxonomyTaxRowAdded(TaxonomyEvent.TaxonomyTaxRowAdded added) {
        CCFLog.info(logger, "Taxonomy tax row added", Map.of("taxonomy", added.taxRowAdd().toString()));
        var rows = this.rows();
        rows.add(added.taxRowAdd().row());
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
        rows.removeAll(rowsToRemove);
        return rows;
    }
    public Taxonomy onTaxonomyTaxRowRemoved(TaxonomyEvent.TaxonomyTaxRowRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax row removed", Map.of("taxonomy", removed.rowId()));
        List<TaxRow> newRows = removeTaxonomyRows(List.of(removed.rowId()));
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                newRows);
    }

    public Taxonomy onTaxonomyTaxRowsAdded(TaxonomyEvent.TaxonomyTaxRowsAdded added) {
        CCFLog.info(logger, "Taxonomy tax rows added", Map.of("taxonomy", added.taxRowsAdd().toString()));
        var rows = this.rows();
        if (added.taxRowsAdd().isReplace()) {
            rows.clear();
            rows.addAll(added.taxRowsAdd().rows());
        } else {
            rows.addAll(added.taxRowsAdd().rows());
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                rows);
    }

    public Taxonomy onTaxonomyTaxRowsRemoved(TaxonomyEvent.TaxonomyTaxRowsRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax rows removed", Map.of("taxonomy", removed.taxRowsRemove().toString()));
        List<TaxRow> newRows = removeTaxonomyRows(removed.taxRowsRemove().rowIds());
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(),
                newRows);
    }

    public Taxonomy onTaxonomyTaxRowUpdated(TaxonomyEvent.TaxonomyTaxRowUpdated updated) {
        CCFLog.info(logger, "Taxonomy tax row updated", Map.of("taxonomy", updated.taxRowUpdate().toString()));
        var rows = this.rows();
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
