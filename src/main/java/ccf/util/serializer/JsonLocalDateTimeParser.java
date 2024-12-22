package ccf.util.serializer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

public class JsonLocalDateTimeParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonLocalDateTimeParser.class);

    public static LocalDateTime parseLocalDateTimeJSON(JsonParser jsonParser,
                                                       LocalDateTime ldt, boolean insidePropertiesObj) throws JsonParseException, IOException {
        logger.info("Parsing LocalDateTimeJSON ldt:{}", ldt);
        int newYear = 0;
        long newClicks = 0;
        LocalDateTime ldt1 = LocalDateTime.now();
        logger.info("Parsing DateTimeJSON entering ldt1: {}", ldt1);

        //loop through the JsonTokens
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String name = jsonParser.getCurrentName();
            if ("year".equals(name)) {
                jsonParser.nextToken();
                newYear = jsonParser.getIntValue();
            }
            else if ("clicks".equals(name)) {
                jsonParser.nextToken();
                newClicks = jsonParser.getLongValue();
//                dt1 = LocalDateTime.create(newClicks);
            }
            else {
                jsonParser.nextToken();
                logger.info("Parsing DateTimeJSON: name: {}", name);
            }
        }
        logger.info("Parsing LocalDateTimeJSON exiting ldt1: {}", ldt1);
        return ldt;
    }
}