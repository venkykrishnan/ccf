package ccf.domain.standard;

public record StandardVersion(Integer major, Integer minor) {
    public StandardVersion () {
        this(1,    0);
    }
    public String version() {
        return major + "." + minor;
    }
    public String versionId(String name) {
        return name + "-" + major + "." + minor;
    }
}
