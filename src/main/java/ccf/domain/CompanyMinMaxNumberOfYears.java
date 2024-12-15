package ccf.domain;

public enum CompanyMinMaxNumberOfYears {
    MIN_NUMBER_OF_YEARS(1),
    MAX_NUMBER_OF_YEARS(10);

    private final int value;

    CompanyMinMaxNumberOfYears(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

