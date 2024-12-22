package ccf.util;

import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Map;
//import java.util.logging.Level;


public class CCFLog {
    public static void warn(Logger logger, String message, Map<String,String> mdc) {
        mdc.forEach(MDC::put);
        logger.warn(message);
        MDC.clear();
    }
    public static void error(Logger logger, String message, Map<String,String> mdc) {
        mdc.forEach(MDC::put);
        logger.error(message);
        MDC.clear();
    }
    public static void info(Logger logger, String message, Map<String,String> mdc) {
        mdc.forEach(MDC::put);
        logger.info(message);
        MDC.clear();
    }
    public static void debug(Logger logger, String message, Map<String,String> mdc) {
        mdc.forEach(MDC::put);
        logger.debug(message);
        MDC.clear();
    }
    public static void trace(Logger logger, String message, Map<String,String> mdc) {
        mdc.forEach(MDC::put);
        logger.trace(message);
        MDC.clear();
    }
}