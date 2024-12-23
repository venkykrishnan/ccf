package ccf.util.serializer;

import ccf.util.serializer.CustomInstantModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CustomObjectMapperConfig {

    public static ObjectMapper createCustomObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new CustomInstantModule());
        return objectMapper;
    }
}