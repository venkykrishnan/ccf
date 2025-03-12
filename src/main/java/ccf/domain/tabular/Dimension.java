package ccf.domain.tabular;

import ccf.domain.bank.BankEvent;
import ccf.domain.bank.BankInstanceType;
import ccf.domain.bank.BankStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

// HIA: 10 Mar 25 TODO work on standard dimensions & types
public record Dimension(String name, String description) {
}

public record DimensionValue(String name, String description, String type, String parent) {
}

public record DimensionValues(List<DimensionValue> values) {
}
public record DimensionWithValues(Dimension dimension, DimensionValues values) {
}

public record DimensionType(String name, String description, String parent) {
}


