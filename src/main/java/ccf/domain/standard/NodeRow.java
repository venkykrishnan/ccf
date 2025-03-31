package ccf.domain.standard;

import java.util.List;
import java.util.Map;

public record NodeRow(String value, String description, List<String> aliases, List<String> keywords,
                Map<String, List<String>> dimensionSrcHints, // columns in src dimension table. key is the dimension name, value is the list of column names.
                String parent, List<String> children) {
}
