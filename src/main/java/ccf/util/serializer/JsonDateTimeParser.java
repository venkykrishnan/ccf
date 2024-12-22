package ccf.util.serializer;
import akka.http.javadsl.model.DateTime;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonDateTimeParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonDateTimeParser.class);

    public static DateTime parseDateTimeJSON(JsonParser jsonParser, DateTime dt, boolean insidePropertiesObj) throws JsonParseException, IOException {
        logger.info("Parsing DateTimeJSON dt:{}, year: {}", dt.toIsoDateTimeString(), dt.year());
        int newYear = 0;
        long newClicks = 0;
        DateTime dt1 = DateTime.now();
        logger.info("Parsing DateTimeJSON entering dt1: {}", dt1.toIsoDateTimeString());

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
                dt1 = DateTime.create(newClicks);
            }
            else {
                jsonParser.nextToken();
                logger.info("Parsing DateTimeJSON: name: {}", name);
            }
        }
        logger.info("Parsing DateTimeJSON exiting dt1: {}, newYear: {}", dt1.toIsoDateTimeString(), newYear);
        return dt;
    }
}