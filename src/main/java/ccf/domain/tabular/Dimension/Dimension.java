package ccf.domain.tabular.Dimension;

import java.util.List;

public record Dimension(String name, String description, List<String> Domain, DimensionType type) {
    public Dimension(String name, String description, List<String> Domain) {
        this(name, description, Domain, DimensionType.STRING);
    }
}
