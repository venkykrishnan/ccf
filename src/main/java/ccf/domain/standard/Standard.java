package ccf.domain.standard;

/*
 * A standard maintains
 * (1) a list of standard domains,
 * (2) a list of valid standard dimensions,
 * (3) a list of taxonomies, each of which is associated with a standard dimension.
 */

import java.util.List;

public record Standard(String name, String description,
                       List<StandardDomain> domains,
                       List<StandardDimension> dimension) {

}