package ccf.util.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import akka.http.javadsl.model.DateTime;

public class AkkaDateTimeModule extends SimpleModule {
    public AkkaDateTimeModule() {
//        addSerializer(DateTime.class, new AkkaDateTimeSerializer());
        addDeserializer(DateTime.class, new AkkaDateTimeDeserializer());
    }
}
