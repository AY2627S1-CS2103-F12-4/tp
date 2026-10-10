package seedu.address.model.mapping;

import java.util.Set;

/**
 * Provides an unmodifiable view of participation mappings.
 */
public interface ReadOnlyMappings {

    /**
     * Returns an unmodifiable snapshot of all valid participation mappings.
     * Mappings whose event or person cannot be found are omitted.
     */
    Set<Participation> getMappings();
}
