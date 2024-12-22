package ccf.util.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

public class AkkaLocalDateTimeModule extends SimpleModule {
    public AkkaLocalDateTimeModule() {
//        addSerializer(DateTime.class, new AkkaDateTimeSerializer());
        addDeserializer(LocalDateTime.class, new AkkaLocalDateTimeDeserializer());
    }
}
