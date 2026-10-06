package seedu.address.model.mapping.exceptions;

/**
 * Signals that an event-person mapping cannot be found.
 */
public class MappingNotFoundException extends RuntimeException {

    /**
     * Creates an exception for the missing event-person mapping.
     */
    public MappingNotFoundException(String eventId, String personId) {
        super(String.format("No mapping exists for event %s and person %s", eventId, personId));
    }
}
