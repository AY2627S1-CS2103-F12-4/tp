package seedu.address.model.event.exceptions;

/**
 * Signals that an operation cannot find the specified event.
 */
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException() {
        super("The specified event could not be found");
    }
}
