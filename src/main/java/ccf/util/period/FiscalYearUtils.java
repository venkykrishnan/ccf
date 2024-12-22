package ccf.util.period;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class FiscalYearUtils {
    public record GetPeriodOpsRequest(FiscalOperators method, FiscalAsOf asOf, int additionalParam) {}

    public static Object invokeMethod(GetPeriodOpsRequest getPeriodsOpsRequest, FiscalYears fiscalYears,
                                      Instant publishPeriod) {
        Instant currentInstant = switch (getPeriodsOpsRequest.asOf) {
            case PUBLISHED_DATE -> publishPeriod;
            case CURRENT_DATE -> Instant.now();
            case LAST_MONTH -> getPreviousMonth(fiscalYears, Instant.now());
            case THIS_QUARTER -> getCurrentQuarterMonths(fiscalYears, Instant.now());
            case LAST_YEAR -> getPreviousMonthPreviousYear(fiscalYears, Instant.now());
        };
        return invokeMethod(getPeriodsOpsRequest.method, fiscalYears, currentInstant, getPeriodsOpsRequest.additionalParam);
    }
    public static Object invokeMethod(FiscalOperators method, FiscalYears fiscalYears, Instant currentInstant, int additionalParam) {
        return switch (method) {
            case PREVIOUS_MONTH -> getPreviousMonth(fiscalYears, currentInstant);
            case PREVIOUS_MONTH_PREVIOUS_YEAR -> getPreviousMonthPreviousYear(fiscalYears, currentInstant);
            case CURRENT_QUARTER_MONTHS ->getCurrentQuarterMonths(fiscalYears, currentInstant);
            case CURRENT_QUARTER_ORDINAL -> getCurrentQuarterOrdinal(fiscalYears, currentInstant);
            case QUARTER_BY_NUMBER -> getQuarterByNumber(fiscalYears, currentInstant, additionalParam);
            case PREVIOUS_QUARTER -> getPreviousQuarter(fiscalYears, currentInstant);
            case CURRENT_YEAR_ORDINAL -> getCurrentYearOrdinal(fiscalYears, currentInstant);
            case CURRENT_YEAR_MONTHS -> getCurrentYearMonths(fiscalYears, currentInstant);
            case YEAR_TO_DATE -> getYearToDate(fiscalYears, currentInstant);
            case QUARTER_TO_DATE -> getQuarterToDate(fiscalYears, currentInstant);
            case YEAR_OVER_YEAR -> getYearOverYear(fiscalYears, currentInstant);
        };
    }

    // 1. Previous month
    private static Instant getPreviousMonth(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear firstFiscalYear = fiscalYears.fiscalYears().getFirst();
        Instant firstMonth = firstFiscalYear.months().getFirst();

        if (currentInstant.equals(firstMonth)) {
            throw new IllegalStateException("Cannot get previous month as this is the first month of the first fiscal year.");
        }

        return currentInstant.atZone(ZoneId.systemDefault())
                .minusMonths(1)
                .toInstant();
    }

    // 2. Previous month of previous year
    private static Instant getPreviousMonthPreviousYear(FiscalYears fiscalYears, Instant currentInstant) {
        ZonedDateTime currentDateTime = currentInstant.atZone(ZoneId.systemDefault());
        int previousYearValue = currentDateTime.getYear() - 1;

        if (previousYearValue < fiscalYears.startYear().getValue()) {
            throw new IllegalStateException("No previous year exists in the FiscalYears range.");
        }

        return currentDateTime
                .minusYears(1)
                .toInstant();
    }

    // 3. Current quarter starting month (returns an Instant)
    private static Instant getCurrentQuarterMonths(FiscalYears fiscalYears,  Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        Month currentMonth = currentInstant.atZone(ZoneId.systemDefault()).getMonth();

        int currentQuarterIndex = (currentMonth.ordinal() - currentFiscalYear.startMonth().ordinal() + 12) / 3 % 4;
        return currentFiscalYear.quarters().get(currentQuarterIndex); // First month of the quarter
    }

    // 4. Current quarter (ordinal from 1 to 4)
    private static Integer getCurrentQuarterOrdinal(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        Month currentMonth = currentInstant.atZone(ZoneId.systemDefault()).getMonth();

        return ((currentMonth.ordinal() - currentFiscalYear.startMonth().ordinal() + 12) / 3 % 4) + 1;
    }

    // 5. Get quarter by number (returns starting Instant of the quarter)
    private static Instant getQuarterByNumber(FiscalYears fiscalYears, Instant currentInstant, int quarterNumber) {
        if (quarterNumber < 1 || quarterNumber > 4) {
            throw new IllegalArgumentException("Quarter number must be between 1 and 4.");
        }

        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        return currentFiscalYear.quarters().get(quarterNumber - 1); // First month of the quarter
    }

    // 6. Previous quarter starting month (returns an Instant)
    private static Instant getPreviousQuarter(FiscalYears fiscalYears, Instant currentInstant) {
        int currentQuarter = getCurrentQuarterOrdinal(fiscalYears, currentInstant);
        int previousQuarter = (currentQuarter - 2 + 4) % 4 + 1; // Wrap around to handle quarter 1 going to 4
        return getQuarterByNumber(fiscalYears, currentInstant, previousQuarter);
    }

    // 7. Current year ordinal in FiscalYears (converted to 1-based)
    private static Integer getCurrentYearOrdinal(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        return fiscalYears.fiscalYears().indexOf(currentFiscalYear) + 1;
    }

    // 8. Current year months
    private static List<Instant> getCurrentYearMonths(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        return currentFiscalYear.months();
    }

    // 9. Year to Date
    private static List<Instant> getYearToDate(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        List<Instant> months = currentFiscalYear.months();
        List<Instant> yearToDate = new ArrayList<>();
        for (Instant month : months) {
            if (!month.isAfter(currentInstant)) {
                yearToDate.add(month);
            }
        }
        return yearToDate;
    }

    // 10. Quarter to Date
    private static List<Instant> getQuarterToDate(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        int currentQuarterIndex = getCurrentQuarterOrdinal(fiscalYears, currentInstant) - 1;
        Instant quarterMonth = currentFiscalYear.quarters().get(currentQuarterIndex);
        // Get all months of the current quarter
        List<Instant> quarterMonths = currentFiscalYear.months().subList(quarterMonth.atZone(ZoneId.systemDefault()).getMonth().ordinal(),
                quarterMonth.atZone(ZoneId.systemDefault()).getMonth().ordinal() + 3);

        List<Instant> quarterToDate = new ArrayList<>();
        for (Instant month : quarterMonths) {
            if (!month.isAfter(currentInstant)) {
                quarterToDate.add(month);
            }
        }
        return quarterToDate;
    }

    // 11. Year over Year
    private static Instant getYearOverYear(FiscalYears fiscalYears, Instant currentInstant) {
        FiscalYear currentFiscalYear = getCurrentFiscalYear(fiscalYears, currentInstant);
        int currentYearIndex = fiscalYears.fiscalYears().indexOf(currentFiscalYear);
        if (currentYearIndex == 0) {
            throw new IllegalStateException("No prior fiscal year exists.");
        }

        FiscalYear previousFiscalYear = fiscalYears.fiscalYears().get(currentYearIndex - 1);
        ZonedDateTime currentDateTime = currentInstant.atZone(ZoneId.systemDefault());
        Month currentMonth = currentDateTime.getMonth();

        for (Instant month : previousFiscalYear.months()) {
            if (month.atZone(ZoneId.systemDefault()).getMonth() == currentMonth) {
                return month;
            }
        }

        throw new IllegalStateException("No matching month found in the prior fiscal year.");
    }
    // Helper: Find the current fiscal year based on the current instant
    private static FiscalYear getCurrentFiscalYear(FiscalYears fiscalYears, Instant currentInstant) {
        for (FiscalYear fy : fiscalYears.fiscalYears()) {
            Instant startOfYear = fy.months().getFirst(); // Start of fiscal year
            Instant endOfYear = fy.months().getLast(); // End of fiscal year

            if (!currentInstant.isBefore(startOfYear) && !currentInstant.isAfter(endOfYear)) {
                return fy;
            }
        }
        throw new IllegalStateException("Current instant does not fall into any fiscal year.");
    }

    // Main method for demonstration
    public static void main(String[] args) {
        // Create FiscalYears starting in March 2024 for 2 years
        FiscalYears fiscalYears = new FiscalYears(Year.of(2024), Month.MARCH, 2);

        // Use the current time as the current instant
        Instant now = Instant.now();
        System.out.println("Current Instant: " + now);
        System.out.println("Previous Month: " + getPreviousMonth(fiscalYears, now));
        System.out.println("Previous Month Previous Year: " + getPreviousMonthPreviousYear(fiscalYears, now));
        System.out.println("Current Quarter Starting Month: " + getCurrentQuarterMonths(fiscalYears,now));
        System.out.println("Current Quarter (Ordinal): " + getCurrentQuarterOrdinal(fiscalYears, now));
        System.out.println("Quarter 2 Starting Month: " + getQuarterByNumber(fiscalYears, now, 2));
        System.out.println("Previous Quarter Starting Month: " + getPreviousQuarter(fiscalYears, now));
        System.out.println("Current Year (Ordinal): " + getCurrentYearOrdinal(fiscalYears, now));
        System.out.println("Current Year Months: " + getCurrentYearMonths(fiscalYears, now));
    }
}
