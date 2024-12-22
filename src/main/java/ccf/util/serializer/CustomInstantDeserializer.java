package ccf.util.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

public class CustomInstantDeserializer extends JsonDeserializer<Instant> {
    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String pString = p.getValueAsString();
        return createInstantFromYearMonth(pString);
    }

    public static Instant createInstantFromYearMonth(String input) {
        try {
            // Validate and parse input
            YearMonth yearMonth = YearMonth.parse(input);

            // Get the first day of the month at 00:00:00
            LocalDate firstDay = yearMonth.atDay(1);

            // Convert LocalDate to Instant (UTC time at start of day)
            return firstDay.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new CustomDateTimeParseException("Expecting YYYY-MM format, invalid input: " + input, e);
        }
    }
}

class CustomDateTimeParseException extends RuntimeException {
    public CustomDateTimeParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

