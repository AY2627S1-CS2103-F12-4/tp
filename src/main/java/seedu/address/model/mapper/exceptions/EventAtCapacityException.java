package seedu.address.model.mapper.exceptions;

/**
 * Signals that an event cannot accept another participant because it is at capacity.
 */
public class EventAtCapacityException extends RuntimeException {

    /**
     * Creates an exception for the event that is at capacity.
     */
    public EventAtCapacityException(String eventId) {
        super(String.format("Event %s is at full capacity", eventId));
    }
}
