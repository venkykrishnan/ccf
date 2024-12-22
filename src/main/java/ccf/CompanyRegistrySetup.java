package ccf;

import akka.javasdk.JsonSupport;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import ccf.util.serializer.CustomLocalDateTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;
import com.fasterxml.jackson.dataformat.protobuf.ProtobufMapper;


@Setup
public class CompanyRegistrySetup implements ServiceSetup {

    private static final Logger logger = LoggerFactory.getLogger(CompanyRegistrySetup.class);


    @Override
    public void onStartup() {
        logger.info("Starting Akka Application");

        // Use the statement below if you want to get the currently registered modules
//        logger.info("Registered modules: {}", JsonSupport.getObjectMapper().getRegisteredModuleIds());

        JsonSupport.getObjectMapper()
                // Register a serializer/deserializer if you have your own class/record
                // however this didn't work for DateTime as akka does this before onStartup
//                .registerModule(new CustomLocalDateTimeModule())
//                .registerModule(new CustomLocalDateTimeModule())
                .configure(FAIL_ON_NULL_CREATOR_PROPERTIES, true);
    }
}
