package seedu.address.model.mapper.exceptions;

/**
 * Signals that an event-person participation already exists.
 */
public class DuplicateParticipationException extends RuntimeException {

    /**
     * Creates an exception for the duplicate event-person participation.
     */
    public DuplicateParticipationException(String eventId, String personId) {
        super(String.format("A participation already exists for event %s and person %s", eventId, personId));
    }
}
