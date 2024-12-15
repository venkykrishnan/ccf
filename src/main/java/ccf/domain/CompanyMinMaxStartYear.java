package ccf.domain;

public enum CompanyMinMaxStartYear {
    MIN_START_YEAR(2000),
    MAX_START_YEAR(2030);

    private final int value;

    CompanyMinMaxStartYear(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
