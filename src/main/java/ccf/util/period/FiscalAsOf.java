package ccf.util.period;

import java.util.HashMap;
import java.util.Map;

public enum FiscalAsOf {
    PUBLISHED_DATE("published_date"),
    CURRENT_DATE("current_date"),
    LAST_MONTH("last_month"),
    THIS_QUARTER("this_quarter"),
    LAST_YEAR("last_year");

    private final String value;
    private static final Map<String, FiscalAsOf> STRING_TO_ENUM = new HashMap<>();

    static {
        for (FiscalAsOf asOf : values()) {
            STRING_TO_ENUM.put(asOf.value, asOf);
        }
    }

    FiscalAsOf(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static FiscalAsOf fromString(String value) {
        FiscalAsOf asOf = STRING_TO_ENUM.get(value.toLowerCase());
        if (asOf == null) {
            throw new IllegalArgumentException("Invalid FiscalAsOf: " + value);
        }
        return asOf;
    }
}