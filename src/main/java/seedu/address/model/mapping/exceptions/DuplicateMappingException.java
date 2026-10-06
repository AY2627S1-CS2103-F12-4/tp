package seedu.address.model.mapping.exceptions;

/**
 * Signals that an event-person mapping already exists.
 */
public class DuplicateMappingException extends RuntimeException {

    /**
     * Creates an exception for the duplicate event-person mapping.
     */
    public DuplicateMappingException(String eventId, String personId) {
        super(String.format("A mapping already exists for event %s and person %s", eventId, personId));
    }
}
