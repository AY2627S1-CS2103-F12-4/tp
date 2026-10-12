package seedu.address.model.mapper.exceptions;

/**
 * Signals that an event-person participation cannot be found.
 */
public class ParticipationNotFoundException extends RuntimeException {

    /**
     * Creates an exception for the missing event-person participation.
     */
    public ParticipationNotFoundException(String eventId, String personId) {
        super(String.format("No participation exists for event %s and person %s", eventId, personId));
    }
}
