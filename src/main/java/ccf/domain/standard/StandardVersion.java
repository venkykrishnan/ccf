package ccf.domain.standard;

public record StandardVersion(Integer major, Integer minor) {
    public StandardVersion () {
        this(1,    0);
    }
    public String version() {
        return major + "." + minor;
    }
    public StandardVersion(String version) {
        this(parse(version).major(), parse(version).minor());
    }
    // expect a version string in the format of "major.minor"
    private static StandardVersion parse(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid version string: " + version);
        }
        return new StandardVersion(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }
}
