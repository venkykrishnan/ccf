package ccf.domain.standard;

import java.util.List;

import ccf.domain.standard.StandardDimension.TaxonomyVersion;


public record TaxonomyRow(String name, String description, StandardVersion defaultVersion, List<TaxonomyVersion> taxonomyVersions) {
}
