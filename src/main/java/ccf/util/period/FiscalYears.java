package ccf.util.period;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

public record FiscalYears(Year startYear, Month startMonth, int noOfYears, List<FiscalYear> fiscalYears) {

    // Primary constructor that calculates the list of FiscalYear objects
    public FiscalYears(Year startYear, Month startMonth, int noOfYears) {
        this(startYear, startMonth, noOfYears, generateFiscalYears(startYear, startMonth, noOfYears));
    }

    // Helper method to generate a list of FiscalYear objects
    private static List<FiscalYear> generateFiscalYears(Year startYear, Month startMonth, int noOfYears) {
        List<FiscalYear> fiscalYearList = new ArrayList<>();

        // Generate FiscalYear instances for the specified number of years
        for (int i = 0; i < noOfYears; i++) {
            Year currentYear = startYear.plusYears(i); // Calculate the year for the fiscal year
            fiscalYearList.add(new FiscalYear(currentYear, startMonth));
        }

        return fiscalYearList;
    }

    // Main method for demonstration
    public static void main(String[] args) {
        // Create a FiscalYears instance starting in March 2024 for 3 years
        FiscalYears fiscalYears = new FiscalYears(Year.of(2024), Month.MARCH, 3);

        // Print fiscal years' details
        System.out.println("Start Year: " + fiscalYears.startYear());
        System.out.println("Start Month: " + fiscalYears.startMonth());
        System.out.println("Number of Years: " + fiscalYears.noOfYears());

        System.out.println("\nFiscal Years:");
        for (FiscalYear fy : fiscalYears.fiscalYears()) {
            System.out.println("Fiscal Year: " + fy.year());
            System.out.println("Start Month: " + fy.startMonth());
            System.out.println("Months:");
            for (Instant month : fy.months()) {
                System.out.println("  " + month);
            }
            System.out.println("Quarters:");
            for (Instant quarter : fy.quarters()) {
                System.out.println("  " + quarter);
            }
            System.out.println("----------");
        }
    }
}
