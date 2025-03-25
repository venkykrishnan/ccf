package ccf.domain.standard;

import java.util.List;

import ccf.domain.standard.StandardDimension.StandardDimensionRow;

// TaxonomyRow is a combination of a Taxonomy and a TaxonomyVersion
public record TaxonomyRow(String name, String description, StandardVersion version, List<StandardDimensionRow> rows,
    Boolean isPublished, Boolean isDefault,
    String dimensionName)
    {
}
