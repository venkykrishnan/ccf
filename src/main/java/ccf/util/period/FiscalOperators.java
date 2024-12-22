package ccf.util.period;

import java.util.HashMap;
import java.util.Map;

public enum FiscalOperators {
    PREVIOUS_MONTH("previous_month"),
    PREVIOUS_MONTH_PREVIOUS_YEAR("previous_month_previous_year"),
    CURRENT_QUARTER_MONTHS("current_quarter_months"),
    CURRENT_QUARTER_ORDINAL("current_quarter_ordinal"),
    QUARTER_BY_NUMBER("quarter_by_number"),
    PREVIOUS_QUARTER("previous_quarter"),
    CURRENT_YEAR_ORDINAL("current_year_ordinal"),
    CURRENT_YEAR_MONTHS("current_year_months"),
    YEAR_TO_DATE("year_to_date"),
    QUARTER_TO_DATE("quarter_to_date"),
    YEAR_OVER_YEAR("year_over_year");

    private final String value;
    private static final Map<String, FiscalOperators> STRING_TO_ENUM = new HashMap<>();

    static {
        for (FiscalOperators operator : values()) {
            STRING_TO_ENUM.put(operator.value, operator);
        }
    }

    FiscalOperators(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static FiscalOperators fromString(String value) {
        FiscalOperators operator = STRING_TO_ENUM.get(value.toLowerCase());
        if (operator == null) {
            throw new IllegalArgumentException("Invalid FiscalOperators: " + value);
        }
        return operator;
    }
}