package ccf.domain.standard;

/*
 * A standard maintains
 * (1) a list of standard domains,
 * (2) a list of valid standard dimensions,
 * (3) a list of taxonomies, each of which is associated with a standard dimension.
 */

import ccf.util.CCFLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record Standard(String name, String description,
                       List<StandardDomain> domains,
                       List<StandardDimension> dimensions,
                       StandardStatus status) {
    private static final Logger logger = LoggerFactory.getLogger(Standard.class);

    public record StandardCreate(String name, String description) {
    }

    //<editor-fold desc="Create & Modify">
    public Standard onStandardCreated(StandardEvent.StandardCreated standardCreated) {
        return new Standard(standardCreated.standardCreate().name,
                standardCreated.standardCreate().description, List.of(), List.of(), StandardStatus.STANDARD_INITIALIZED_NO_DOMAINS);
    }

    public Standard onStandardDomainAdded(StandardEvent.StandardDomainAdded standardDomainAdded) throws StandardException {
        if (domains().stream().anyMatch(d -> d.name().equals(standardDomainAdded.standardDomain().name()))) {
            CCFLog.error(logger, "Adding a domain failed, already created",
                    Map.of("name", standardDomainAdded.standardDomain().name()));
            throw new StandardException(standardDomainAdded.standardDomain().name(), "Domain already exists in the standard.");
        }
        var newDomains = new java.util.ArrayList<>(List.copyOf(domains()));
        newDomains.add(standardDomainAdded.standardDomain());
        return new Standard(name(), description(), newDomains, dimensions(),StandardStatus.STANDARD_INITIALIZED);
    }

    public Standard onStandardDimensionAdded(StandardEvent.StandardDimensionAdded standardDimensionAdded) {
        if (dimensions().stream().anyMatch(d -> d.name().equals(standardDimensionAdded.standardDimensionCreate().name()))) {
            CCFLog.error(logger, "Adding a dimension failed, already created",
                    Map.of("name", standardDimensionAdded.standardDimensionCreate().name()));
            throw new IllegalArgumentException("Dimension %s already exists.".formatted(standardDimensionAdded.standardDimensionCreate().name()));
        }
        // Check if the domains listed in the standardDimensionCreate are valid (i.e. they exist in the standard)
        for (var domain : standardDimensionAdded.standardDimensionCreate().domains()) {
            if (domains().stream().noneMatch(d -> d.name().equals(domain))) {
                CCFLog.error(logger, "Adding a dimension failed, domain does not exist",
                        Map.of("name", standardDimensionAdded.standardDimensionCreate().name(),
                                "domain", domain));
                throw new IllegalArgumentException("Domain %s does not exist in the standard.".formatted(domain));
            }
        }
        StandardDimension newDimension = new StandardDimension(
                standardDimensionAdded.standardDimensionCreate().name(),
                standardDimensionAdded.standardDimensionCreate().description(),
                standardDimensionAdded.standardDimensionCreate().domains(),
                Map.of());
        var newDimensions = new java.util.ArrayList<>(List.copyOf(dimensions()));
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,StandardStatus.STANDARD_INITIALIZED);
    }

    public Standard onStandardTaxonomyAdded(StandardEvent.StandardTaxonomyAdded standardTaxonomyAdded) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy already exists (if it does, throw an exception)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardTaxonomyAdded.taxonomyCreate().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardTaxonomyAdded.taxonomyCreate().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardTaxonomyAdded.taxonomyCreate().name());
        if (taxonomy != null) {
            CCFLog.error(logger, "Adding a taxonomy failed, already created",
                    Map.of("name", standardTaxonomyAdded.taxonomyCreate().name()));
            throw new IllegalArgumentException("Taxonomy %s already exists.".formatted(standardTaxonomyAdded.taxonomyCreate().name()));
        }

        var newDimensions = getStandardDimensions(standardTaxonomyAdded, dimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    private ArrayList<StandardDimension> getStandardDimensions(StandardEvent.StandardTaxonomyAdded standardTaxonomyAdded, StandardDimension dimension) {
        var newTaxonomy = new StandardDimension.Taxonomy(
                standardTaxonomyAdded.taxonomyCreate().name(),
                standardTaxonomyAdded.taxonomyCreate().description(),
                null,
                List.of());
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.put(standardTaxonomyAdded.taxonomyCreate().name(), newTaxonomy);
        var newDimension = new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies
        );
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return newDimensions;
    }

    public Standard onStandardTaxonomyVersionAdded(StandardEvent.StandardTaxonomyVersionAdded standardTaxonomyVersionAdded) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version already exists (if it does, throw an exception)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardTaxonomyVersionAdded.taxonomyVersionCreate().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardTaxonomyVersionAdded.taxonomyVersionCreate().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardTaxonomyVersionAdded.taxonomyVersionCreate().taxonomyName());
        if (taxonomy == null) {
            CCFLog.error(logger, "Adding a taxonomy version failed, taxonomy does not exist",
                    Map.of("name", standardTaxonomyVersionAdded.taxonomyVersionCreate().taxonomyName()));
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardTaxonomyVersionAdded.taxonomyVersionCreate().taxonomyName()));
        }

        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardTaxonomyVersionAdded.taxonomyVersionCreate().standardVersion().version()))
                .findFirst()
                .orElse(null);
        if (taxonomyVersion != null) {
            CCFLog.error(logger, "Adding a taxonomy version failed, taxonomy version already exists",
                    Map.of("name", standardTaxonomyVersionAdded.taxonomyVersionCreate().taxonomyName()));
            throw new IllegalArgumentException("Version %s already exists.".formatted(
                    standardTaxonomyVersionAdded.taxonomyVersionCreate().standardVersion().version()));
        }

        var newDimensions = getStandardDimensions(standardTaxonomyVersionAdded, taxonomy, dimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    private ArrayList<StandardDimension> getStandardDimensions(StandardEvent.StandardTaxonomyVersionAdded standardTaxonomyVersionAdded, StandardDimension.Taxonomy taxonomy, StandardDimension dimension) {
        var newRows = new ArrayList<StandardDimension.StandardDimensionRow>();
        var newVersion = new StandardDimension.TaxonomyVersion(
                standardTaxonomyVersionAdded.taxonomyVersionCreate().standardVersion(),
                false,
                newRows
        );
        var newDimension = getStandardDimension(taxonomy, dimension, newVersion);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return newDimensions;
    }

    private static StandardDimension getStandardDimension(StandardDimension.Taxonomy taxonomy, StandardDimension dimension, StandardDimension.TaxonomyVersion newVersion) {
        var newVersions = new ArrayList<>(taxonomy.taxonomyVersions());
        newVersions.add(newVersion);
        var newTaxonomy = new StandardDimension.Taxonomy(
                taxonomy.name(),
                taxonomy.description(),
                taxonomy.defaultVersion(),
                newVersions
        );
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.put(taxonomy.name(), newTaxonomy);
        return new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies
        );
    }

    public Standard onStandardDimensionRowAdded(StandardEvent.StandardDimensionRowAdded standardDimensionRowAdded) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Then check if the row already exists (if it does, replace it)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardDimensionRowAdded.rowAdd().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardDimensionRowAdded.rowAdd().dimensionName())));

        var taxonomy = dimension.taxonomies().get(standardDimensionRowAdded.rowAdd().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardDimensionRowAdded.rowAdd().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardDimensionRowAdded.rowAdd().versionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardDimensionRowAdded.rowAdd().taxonomyName())));
        // check if this taxonomyVersion has isPublished = true and if so throw an exception
        if (taxonomyVersion.isPublished()) {
            logger.info("Cannot add row to a published taxonomy version.");
            throw new IllegalArgumentException("Cannot add row to a published taxonomy version.");
        }
        var newRows = new ArrayList<>(taxonomyVersion.rows());

        // check if the new row's parent is already in the list, if not throw an exception
        if (newRows.stream().anyMatch(r -> r.value().equals(standardDimensionRowAdded.rowAdd().row().parent()))) {
            logger.info("Parent row does not exist.");
            throw new IllegalArgumentException("Parent row %s does not exist.".formatted(standardDimensionRowAdded.rowAdd().row().parent()));
        }

        newRows.stream()
                .filter(r -> r.value().equals(standardDimensionRowAdded.rowAdd().row().value()))
                .findFirst().ifPresent(newRows::remove);
        newRows.add(standardDimensionRowAdded.rowAdd().row());
        var newVersion = new StandardDimension.TaxonomyVersion(
                taxonomyVersion.version(),
                false,
                newRows
        );
        var newDimension = getStandardDimension(taxonomy, dimension, newVersion);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardDimensionRowsAdded(StandardEvent.StandardDimensionRowsAdded standardDimensionRowsAdded) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Then check if the rows already exist (if they do, replace them)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardDimensionRowsAdded.rowsAdd().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardDimensionRowsAdded.rowsAdd().dimensionName())));

        var taxonomy = dimension.taxonomies().get(standardDimensionRowsAdded.rowsAdd().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardDimensionRowsAdded.rowsAdd().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardDimensionRowsAdded.rowsAdd().versionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardDimensionRowsAdded.rowsAdd().versionName())));
        // check if this taxonomyVersion has isPublished = true and if so throw an exception
        if (taxonomyVersion.isPublished()) {
            logger.info("Cannot add rows to a published taxonomy version.");
            throw new IllegalArgumentException("Cannot add rows to a published taxonomy version.");
        }

        var newRows = new ArrayList<>(taxonomyVersion.rows());

        for (var row : standardDimensionRowsAdded.rowsAdd().rows()) {
            // check if the new row's parent is already in the list, if not throw an exception
            if (newRows.stream().noneMatch(r -> r.value().equals(row.parent()))) {
                logger.info("Parent row does not exist.");
                throw new IllegalArgumentException("Parent row %s does not exist.".formatted(row.parent()));
            }

            newRows.stream()
                    .filter(r -> r.value().equals(row.value()))
                    .findFirst().ifPresent(newRows::remove);
            newRows.add(row);
        }

        var newVersion = new StandardDimension.TaxonomyVersion(
                taxonomyVersion.version(),
                false,
                newRows
        );
        var newDimension = getStandardDimension(taxonomy, dimension, newVersion);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardTaxonomyDefaultVersionSet(StandardEvent.StandardTaxonomyDefaultVersionSet defaultVersionSet) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(defaultVersionSet.taxonomyVersionDefault().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(defaultVersionSet.taxonomyVersionDefault().dimensionName())));
        var taxonomy = dimension.taxonomies().get(defaultVersionSet.taxonomyVersionDefault().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(defaultVersionSet.taxonomyVersionDefault().taxonomyName()));
        }
        taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        defaultVersionSet.taxonomyVersionDefault().standardVersion().version()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(defaultVersionSet.taxonomyVersionDefault().standardVersion().version())));

        var newTaxonomy = new StandardDimension.Taxonomy(
                taxonomy.name(),
                taxonomy.description(),
                defaultVersionSet.taxonomyVersionDefault().standardVersion(),
                taxonomy.taxonomyVersions()
        );
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.put(taxonomy.name(), newTaxonomy);
        var newDimension = new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies
        );
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardTaxonomyPublished(StandardEvent.StandardTaxonomyPublish standardTaxonomyPublish) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Update the version's publish status to the input parameter provided
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardTaxonomyPublish.taxonomyVersionPublish().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardTaxonomyPublish.taxonomyVersionPublish().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardTaxonomyPublish.taxonomyVersionPublish().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardTaxonomyPublish.taxonomyVersionPublish().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardTaxonomyPublish.taxonomyVersionPublish().standardVersion().version()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardTaxonomyPublish.taxonomyVersionPublish().standardVersion().version())));
        var newTaxonomyVersion = new StandardDimension.TaxonomyVersion(
                taxonomyVersion.version(),
                standardTaxonomyPublish.taxonomyVersionPublish().isPublished(),
                taxonomyVersion.rows()
        );
        // Now the taxonomyVersions needs to be updated with this new version
        var newVersions = new ArrayList<>(taxonomy.taxonomyVersions());
        newVersions.removeIf(v -> v.version().version().equals(taxonomyVersion.version().version()));
        newVersions.add(newTaxonomyVersion);

        var newTaxonomy = new StandardDimension.Taxonomy(
                taxonomy.name(),
                taxonomy.description(),
                taxonomy.defaultVersion(),
                newVersions
        );
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.put(taxonomy.name(), newTaxonomy);
        var newDimension = new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies
        );
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }
    //</editor-fold>

    //<editor-fold desc="Clean Up">
    public Standard onStandardDimensionRowRemoved(StandardEvent.StandardDimensionRowRemoved standardDimensionRowRemoved) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Then check if the row already exists (if it does, remove it)
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardDimensionRowRemoved.dimensionRowRemove().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardDimensionRowRemoved.dimensionRowRemove().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardDimensionRowRemoved.dimensionRowRemove().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardDimensionRowRemoved.dimensionRowRemove().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardDimensionRowRemoved.dimensionRowRemove().versionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardDimensionRowRemoved.dimensionRowRemove().versionName())));
        // check if this taxonomyVersion has isPublished = true and if so throw an exception
        if (taxonomyVersion.isPublished()) {
            logger.info("Cannot remove row from a published taxonomy version.");
            throw new IllegalArgumentException("Cannot remove row from a published taxonomy version.");
        }
        var newRows = new ArrayList<>(taxonomyVersion.rows());
        newRows.removeIf(r -> r.value().equals(standardDimensionRowRemoved.dimensionRowRemove().value()));
        var newVersion = new StandardDimension.TaxonomyVersion(
                taxonomyVersion.version(),
                false,
                newRows
        );
        var newDimension = getStandardDimension(taxonomy, dimension, newVersion);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardDimensionRowsRemoved(StandardEvent.StandardDimensionRowsRemoved standardDimensionRowsRemoved) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Then make the rows empty
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardDimensionRowsRemoved.dimensionRowsRemove().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardDimensionRowsRemoved.dimensionRowsRemove().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardDimensionRowsRemoved.dimensionRowsRemove().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardDimensionRowsRemoved.dimensionRowsRemove().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardDimensionRowsRemoved.dimensionRowsRemove().version().version()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardDimensionRowsRemoved.dimensionRowsRemove().version().version())));
        // check if this taxonomyVersion has isPublished = true and if so throw an exception
        if (taxonomyVersion.isPublished()) {
            logger.info("Cannot remove rows from a published taxonomy version.");
            throw new IllegalArgumentException("Cannot remove rows from a published taxonomy version.");
        }
        var newVersion = new StandardDimension.TaxonomyVersion(
                taxonomyVersion.version(),
                false,
                List.of()
        );
        var newDimension = getStandardDimension(taxonomy, dimension, newVersion);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardTaxonomyVersionRemoved(StandardEvent.StandardTaxonomyVersionRemoved standardTaxonomyVersionRemoved) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the version exists (if it doesn't, throw an exception)
        // Then check if the version is the default version (if it is, throw an exception)
        // Then remove the version
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardTaxonomyVersionRemoved.taxonomyVersionRemove().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardTaxonomyVersionRemoved.taxonomyVersionRemove().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardTaxonomyVersionRemoved.taxonomyVersionRemove().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardTaxonomyVersionRemoved.taxonomyVersionRemove().taxonomyName()));
        }
        var taxonomyVersion = taxonomy.taxonomyVersions().stream()
                .filter(v -> v.version().version().equals(
                        standardTaxonomyVersionRemoved.taxonomyVersionRemove().version().version()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("TaxonomyVersion %s does not exist.".formatted(standardTaxonomyVersionRemoved.taxonomyVersionRemove().version().version())));
        if (taxonomy.defaultVersion().version().equals(standardTaxonomyVersionRemoved.taxonomyVersionRemove().version().version())) {
            logger.info("Cannot remove the default version.");
            throw new IllegalArgumentException("Cannot remove the default version.");
        }
        var newVersions = new ArrayList<>(taxonomy.taxonomyVersions());
        newVersions.removeIf(v -> v.version().version().equals(standardTaxonomyVersionRemoved.taxonomyVersionRemove().version().version()));
        var newTaxonomy = new StandardDimension.Taxonomy(
                taxonomy.name(),
                taxonomy.description(),
                taxonomy.defaultVersion(),
                newVersions
        );
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.put(taxonomy.name(), newTaxonomy);
        var newDimension = new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardTaxonomyRemoved(StandardEvent.StandardTaxonomyRemoved standardTaxonomyRemoved) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the taxonomy exists (if it doesn't, throw an exception)
        // Then check if the taxonomy has any versions (if it does, throw an exception)
        // Then remove the taxonomy
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardTaxonomyRemoved.taxonomyRemove().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardTaxonomyRemoved.taxonomyRemove().dimensionName())));
        var taxonomy = dimension.taxonomies().get(standardTaxonomyRemoved.taxonomyRemove().taxonomyName());
        if (taxonomy == null) {
            logger.info("Taxonomy does not exist in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s does not exist.".formatted(standardTaxonomyRemoved.taxonomyRemove().taxonomyName()));
        }
        if (!taxonomy.taxonomyVersions().isEmpty()) {
            logger.info("Taxonomy has versions.");
            throw new IllegalArgumentException("Taxonomy %s has versions.".formatted(standardTaxonomyRemoved.taxonomyRemove().taxonomyName()));
        }
        var newTaxonomies = new java.util.HashMap<>(dimension.taxonomies());
        newTaxonomies.remove(taxonomy.name());
        var newDimension = new StandardDimension(
                dimension.name(),
                dimension.description(),
                dimension.domains(),
                newTaxonomies);
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions,status());
    }

    public Standard onStandardDimensionRemoved(StandardEvent.StandardDimensionRemoved standardDimensionRemoved) {
        // First check if the dimension exists (if it doesn't, throw an exception)
        // Then check if the dimension has any taxonomies (if it does, throw an exception)
        // Then remove the dimension
        var dimension = dimensions().stream()
                .filter(d -> d.name().equals(standardDimensionRemoved.dimensionRemove().dimensionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Dimension %s does not exist.".formatted(standardDimensionRemoved.dimensionRemove().dimensionName())));
        if (!dimension.taxonomies().isEmpty()) {
            logger.info("Dimension has taxonomies.");
            throw new IllegalArgumentException("Dimension %s has taxonomies.".formatted(standardDimensionRemoved.dimensionRemove().dimensionName()));
        }
        var newDimensions = new ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        return new Standard(name(), description(), domains(), newDimensions, StandardStatus.STANDARD_INITIALIZED);
    }

    public Standard onStandardDomainRemoved(StandardEvent.StandardDomainRemoved standardDomainRemoved) {
        // First check if the domain exists (if it doesn't, throw an exception)
        // Then check if the domain is associated with any dimensions (if it is, throw an exception)
        // Then remove the domain
        var domain = domains().stream()
                .filter(d -> d.name().equals(standardDomainRemoved.domainRemove().name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Domain %s does not exist.".formatted(standardDomainRemoved.domainRemove().name())));
        if (dimensions().stream().anyMatch(d -> d.domains().contains(domain.name()))) {
            logger.info("Domain is associated with dimensions.");
            throw new IllegalArgumentException("Domain %s is associated with dimensions.".formatted(standardDomainRemoved.domainRemove().name()));
        }
        var newDomains = new ArrayList<>(List.copyOf(domains()));
        newDomains.remove(domain);
        if (newDomains.isEmpty()) {
            return new Standard(name(), description(), newDomains, dimensions(), StandardStatus.STANDARD_INITIALIZED_NO_DOMAINS);
        }
        return new Standard(name(), description(), newDomains, dimensions(), StandardStatus.STANDARD_INITIALIZED);
    }
    //</editor-fold>
}