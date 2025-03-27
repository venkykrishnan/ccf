package ccf.domain.standard;

public record TaxonomyVersion(Integer major, Integer minor) {
    public TaxonomyVersion () {
        this(1,    0);
    }
    @Override
    public String toString() {
        return major + "." + minor;
    }
    public TaxonomyVersion(String version) {
        this(parse(version).major(), parse(version).minor());
    }
    // expect a version string in the format of "major.minor"
    private static TaxonomyVersion parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid version string: " + version);
        }
        return new TaxonomyVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}

