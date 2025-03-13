package ccf.domain.tabular.Dimension;

public record DimensionValue(String name, String description, String value, String domain) {
    public DimensionValue(String name, String description, String value) {
        this(name, description, value, null);
    }
    public DimensionValue(String name, String value) {
        this(name, null, value);
    }
    public DimensionValue(String name) {
        this(name, null, null);
    }
}