package ccf.util.serializer;

import ccf.util.period.FiscalOperators;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class FiscalOperatorsDeserializer extends JsonDeserializer<FiscalOperators> {
    @Override
    public FiscalOperators deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().toUpperCase();
        return FiscalOperators.valueOf(value);
    }
}