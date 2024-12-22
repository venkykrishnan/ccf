package ccf.util.serializer;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class AkkaLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String localDateTimeString = p.getText();
        LocalDateTime dt = LocalDateTime.now();
        LocalDateTime newDT = JsonLocalDateTimeParser.parseLocalDateTimeJSON(p, dt, false);
        return LocalDateTime.now();
//        return DateTime.fromIsoDateTimeString(dateTimeString).get();
//                parse(dateTimeString); // Parses to Akka DateTime
    }
}

