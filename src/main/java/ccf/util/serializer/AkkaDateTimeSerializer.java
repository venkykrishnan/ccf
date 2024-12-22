package ccf.util.serializer;

import akka.http.javadsl.model.DateTime;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class AkkaDateTimeSerializer extends JsonSerializer<DateTime> {

    @Override
    public void serialize(DateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // Serialize DateTime as an ISO-8601 string
//        gen.writeString(value.toIsoDateTimeString());
        long epochMillis = value.clicks();
        gen.writeNumber(epochMillis);
    }
}
