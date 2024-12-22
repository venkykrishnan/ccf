package ccf.util.period;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public record FiscalYear(Year year, Month startMonth, List<Instant> months, List<Instant> quarters) {

    // Primary constructor that calculates months and quarters
    public FiscalYear(Year year, Month startMonth) {
        this(year, startMonth, generateMonthlyInstants(year, startMonth), generateQuarterInstants(year, startMonth));
    }

    // Helper method to generate the list of Instants for 12 months
    private static List<Instant> generateMonthlyInstants(Year year, Month startMonth) {
        List<Instant> instants = new ArrayList<>();

        // Loop for 12 months, starting from the given month
        for (int i = 0; i < 12; i++) {
            Month currentMonth = startMonth.plus(i);
            LocalDateTime startOfMonth = LocalDateTime.of(year.getValue(), currentMonth, 1, 0, 0);
            Instant instant = startOfMonth.atZone(ZoneId.systemDefault()).toInstant();
            instants.add(instant);
        }

        return instants;
    }

    // Helper method to generate the list of Instants for each quarter
    private static List<Instant> generateQuarterInstants(Year year, Month startMonth) {
        List<Instant> quarters = new ArrayList<>();

        // Quarters start every 3 months from the start month
        for (int i = 0; i < 4; i++) {
            Month quarterStartMonth = startMonth.plus(i * 3); // Add 3 months for each quarter
            LocalDateTime startOfQuarter = LocalDateTime.of(year.getValue(), quarterStartMonth, 1, 0, 0);
            Instant instant = startOfQuarter.atZone(ZoneId.systemDefault()).toInstant();
            quarters.add(instant);
        }

        return quarters;
    }

    // Main method for demonstration
    public static void main(String[] args) {
        // Create a FiscalYear starting in March 2024
        FiscalYear fiscalYear = new FiscalYear(Year.of(2024), Month.MARCH);

        // Print out fiscal year details
        System.out.println("Fiscal Year: " + fiscalYear.year());
        System.out.println("Start Month: " + fiscalYear.startMonth());

        System.out.println("\nMonths:");
        for (Instant month : fiscalYear.months()) {
            System.out.println(month);
        }

        System.out.println("\nQuarters:");
        for (Instant quarter : fiscalYear.quarters()) {
            System.out.println(quarter);
        }
    }
}
