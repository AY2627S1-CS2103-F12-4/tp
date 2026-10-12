package seedu.address.model.mapper;

import java.util.Set;

/**
 * Provides an unmodifiable view of participations.
 */
public interface ReadOnlyParticipations {

    /**
     * Returns an unmodifiable snapshot of all valid participations.
     * Participations whose event or person cannot be found are omitted.
     */
    Set<Participation> getParticipations();
}
