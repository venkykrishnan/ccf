package ccf;

import akka.javasdk.JsonSupport;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES;

@Setup
public class CompanyRegistrySetup implements ServiceSetup {

    private static final Logger logger = LoggerFactory.getLogger(CompanyRegistrySetup.class);


    @Override
    public void onStartup() {
        logger.info("Starting Akka Application");
        JsonSupport.getObjectMapper()
                .configure(FAIL_ON_NULL_CREATOR_PROPERTIES, true); // <1>
    }
}
