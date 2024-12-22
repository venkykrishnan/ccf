package ccf.util.serializer;

import akka.http.javadsl.model.DateTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class DateTimeDeserializer extends JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String pString = p.getValueAsString();
        String isoString = pString + "T00:00:00";
        return DateTime.fromIsoDateTimeString(isoString).get();
    }
}
