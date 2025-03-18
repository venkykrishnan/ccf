package ccf.domain.standard;

/*
 * A standard maintains
 * (1) a list of standard domains,
 * (2) a list of valid standard dimensions,
 * (3) a list of taxonomies, each of which is associated with a standard dimension.
 */

import ccf.domain.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public record Standard(String name, String description,
                       List<StandardDomain> domains,
                       List<StandardDimension> dimensions) {
    private static final Logger logger = LoggerFactory.getLogger(Standard.class);

    public record StandardCreate(String name, String description) {
    }

    public Standard onStandardCreated(StandardEvent.StandardCreated standardCreated) {
        return new Standard(standardCreated.standardCreate().name,
                standardCreated.standardCreate().description, List.of(), List.of());
    }

    public Standard onStandardDomainAdded(StandardEvent.StandardDomainAdded standardDomainAdded) {
        if (domains().stream().anyMatch(d -> d.name().equals(standardDomainAdded.standardDomain().name()))) {
            logger.info("Domain already exists in the standard.");
            throw new IllegalArgumentException("Domain %s already exists".formatted(standardDomainAdded.standardDomain().name()));
        }
        var newDomains = new java.util.ArrayList<>(List.copyOf(domains()));
        newDomains.add(standardDomainAdded.standardDomain());
        return new Standard(name(), description(), newDomains, dimensions());
    }

    public Standard onStandardDimensionAdded(StandardEvent.StandardDimensionAdded standardDimensionAdded) {
        if (dimensions().stream().anyMatch(d -> d.name().equals(standardDimensionAdded.standardDimensionCreate().name()))) {
            logger.info("Dimension already exists in the standard.");
            throw new IllegalArgumentException("Dimension %s already exists.".formatted(standardDimensionAdded.standardDimensionCreate().name()));
        }
        StandardDimension newDimension = new StandardDimension(
                standardDimensionAdded.standardDimensionCreate().name(),
                standardDimensionAdded.standardDimensionCreate().description(),
                standardDimensionAdded.standardDimensionCreate().domains(),
                Map.of());
        var newDimensions = new java.util.ArrayList<>(List.copyOf(dimensions()));
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions);
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
            logger.info("Taxonomy already exists in the dimension.");
            throw new IllegalArgumentException("Taxonomy %s already exists.".formatted(standardTaxonomyAdded.taxonomyCreate().name()));
        }
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
        var newDimensions = new java.util.ArrayList<>(List.copyOf(dimensions()));
        newDimensions.remove(dimension);
        newDimensions.add(newDimension);
        return new Standard(name(), description(), domains(), newDimensions);
    }
}