package ccf.domain.standard;

public record TaxonomyId(String dimensionName, String name, TaxonomyVersion version) {
    @Override
    public String toString() {
        return dimensionName + "." + name + "." + version;
    }
    public TaxonomyId(String id) {
        this(id.split("\\.")[0], id.split("\\.")[1], new TaxonomyVersion(id.split("\\.")[2]));
        if (id.split("\\.").length != 3) {
            throw new IllegalArgumentException("Invalid taxonomy id: " + id);
        }
    }   
}

