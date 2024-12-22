package ccf.util.serializer;
import akka.http.javadsl.model.DateTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class AkkaDateTimeDeserializer extends JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateTimeString = p.getText();
        DateTime dt = DateTime.now();
        DateTime newDT = JsonDateTimeParser.parseDateTimeJSON(p, dt, false);
        return DateTime.now();
//        return DateTime.fromIsoDateTimeString(dateTimeString).get();
//                parse(dateTimeString); // Parses to Akka DateTime
    }
}

