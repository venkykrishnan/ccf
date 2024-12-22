package ccf.domain;

import ccf.util.serializer.CustomInstantDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.Instant;

public record PublishPeriodRequest(
        @JsonDeserialize(using = CustomInstantDeserializer.class)
        Instant publishedPeriod) {}