package ccf.domain.standard;

public record StandardDomain(String name, String description) {
    public record DomainRemove(String name) {}
}
