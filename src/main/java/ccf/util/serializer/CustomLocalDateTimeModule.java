package ccf.util.serializer;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;

public class CustomLocalDateTimeModule extends SimpleModule {
    public CustomLocalDateTimeModule() {
        super("CustomJavaTimeModule");
        addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
    }
}
