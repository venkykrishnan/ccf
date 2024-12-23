package ccf.util.serializer;

import ccf.CompanyRegistrySetup;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDateTime;

public class CustomInstantModule extends SimpleModule {
    public CustomInstantModule() {

        super("CustomInstantModule", Version.unknownVersion());
        addSerializer(Instant.class, new CustomInstantSerializer());
//        addDeserializer(Instant.class, new CustomInstantDeserializer());
    }
}
