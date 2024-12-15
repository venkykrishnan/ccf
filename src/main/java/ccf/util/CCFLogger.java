package ccf.util;

import ccf.application.CompanyEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;

public class CCFLogger {
    public static void log(Logger logger, String message, Map<String, String> mdc) {
        mdc.forEach(MDC::put);
        logger.info(message);
        MDC.clear();
    }
}
