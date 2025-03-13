package ccf.domain.tabular;

import ccf.domain.tabular.Dimension.Dimension;
import ccf.domain.tabular.domain.Domain;

import java.util.List;

public record tableMetaData(String companyName, List<Domain> domains, List<Dimension> dimensions) {
    public tableMetaData(String companyName, List<Domain> domains) {
        this(companyName, domains, null);
    }
    public tableMetaData(String companyName) {
        this(companyName, null);
    }
    public tableMetaData() {
        this(null);
    }
}
