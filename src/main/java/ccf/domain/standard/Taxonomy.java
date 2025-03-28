package ccf.domain.standard;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ccf.util.CCFLog;


public record Taxonomy(String id, String name, String description, TaxonomyVersion version, String dimension, TaxonomyStatus status, List<TaxRow> rows) {
    private static final Logger logger = LoggerFactory.getLogger(Standard.class);


    public record TaxonomyCreate(String dimensionName, String name, String description, TaxonomyVersion version) {}
    public record TaxRowAdd(String id, TaxRow row) {}
    public record TaxRowRemove(String id, String rowId) {}
    public record TaxRowsAdd(String id, List<TaxRow> rows, Boolean isReplace) {}
    public record TaxRowsRemove(String id, List<String> rowIds) {}
    public record TaxRowUpdate(String id, String rowId, Updates row) {
        public record Updates(String description, List<String> aliases, List<String> keywords, Map<String, List<String>> dimensionSrcHints, String parent) {}
    }

    public record TaxRow(String rowId, String value, String description, List<String> aliases, List<String> keywords, Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the dimension name, value is the list of column names.   
        String parent) {
        public TaxRow(String value, String description, List<String> aliases, List<String> keywords, Map<String, List<String>> dimensionSrcHints, String parent) {
            this(java.util.UUID.randomUUID().toString(), value, description, aliases, keywords, dimensionSrcHints, parent);
        }
    }
    
    public Taxonomy onTaxonomyCreated(TaxonomyEvent.TaxonomyCreated created) {
        CCFLog.info(logger, "Taxonomy created", Map.of("taxonomy", created.taxonomyCreate().toString()));

        if (this.status() != TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id, "Taxonomy already exists");
        }

        return new Taxonomy(this.id, created.taxonomyCreate().name(), created.taxonomyCreate().description(),     
            created.taxonomyCreate().version(), created.taxonomyCreate().dimensionName(), TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
    }

    public Taxonomy onTaxonomyRemoved() {
        CCFLog.info(logger, "Taxonomy removed", Map.of("taxonomy", this.id));
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED || this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot remove - taxonomy is disabled or published");
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), TaxonomyStatus.TAXONOMY_DISABLED, List.<TaxRow>of());
    }

    public Taxonomy onTaxonomyPublished(TaxonomyEvent.TaxonomyPublished published) {
        CCFLog.info(logger, "Taxonomy published", Map.of("taxonomy", this.id, "isPublished", published.isPublish().toString()));
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot publish - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED && published.isPublish()) {
            throw new TaxonomyException(this.id(), "Taxonomy already published");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED && !published.isPublish()) {
            return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), TaxonomyStatus.TAXONOMY_INITIALIZED, List.<TaxRow>of());
        }
        return new Taxonomy(this.id (), this.name(), this.description(), this.version(), this.dimension(), TaxonomyStatus.TAXONOMY_PUBLISHED, this.rows());
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
            throw new TaxonomyException(this.id(), "Tax row with ID '" + added.taxRowAdd().row().rowId() + "' already exists");
        }
        if (rows.stream().anyMatch(row -> row.value().equals(added.taxRowAdd().row().value()))) {
            throw new TaxonomyException(this.id(), "Tax row with value '" + added.taxRowAdd().row().value() + "' already exists");
        }
        rows.add(added.taxRowAdd().row());

        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(), rows);
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
                    throw new TaxonomyException(this.id(), "Tax row with value '" + newRow.value() + "' already exists");
                }
            }
            rows.addAll(added.taxRowsAdd().rows());
        }
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(), rows);
    }
    
    public Taxonomy onTaxonomyTaxRowsRemoved(TaxonomyEvent.TaxonomyTaxRowsRemoved removed) {
        CCFLog.info(logger, "Taxonomy tax rows removed", Map.of("taxonomy", removed.taxRowsRemove().toString()));
        var rows = this.rows();
        if (this.status() == TaxonomyStatus.TAXONOMY_DISABLED) {
            throw new TaxonomyException(this.id(), "Cannot remove tax rows - taxonomy is disabled");
        }
        if (this.status() == TaxonomyStatus.TAXONOMY_PUBLISHED) {
            throw new TaxonomyException(this.id(), "Cannot remove tax rows - taxonomy is published");
        }
        rows.removeAll(removed.taxRowsRemove().rowIds().stream().map(rowId -> rows.stream().filter(row -> row.rowId().equals(rowId))
            .findFirst()
            .orElse(null)).collect(Collectors.toList()));
        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(), rows);
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
        TaxRow row = rows.stream()
                .filter(r -> r.rowId().equals(updated.taxRowUpdate().rowId()))
                .findFirst()
                .orElseThrow(() -> new TaxonomyException(this.id(), "Tax row with ID '" + updated.taxRowUpdate().rowId() + "' not found"));

        rows.set(rows.indexOf(row), new TaxRow(row.rowId(), row.value(), updated.taxRowUpdate().row.description(), 
            updated.taxRowUpdate().row.aliases(), updated.taxRowUpdate().row.keywords(), updated.taxRowUpdate().row.dimensionSrcHints(), updated.taxRowUpdate().row.parent()));

        return new Taxonomy(this.id(), this.name(), this.description(), this.version(), this.dimension(), this.status(), rows);
    }   

}
