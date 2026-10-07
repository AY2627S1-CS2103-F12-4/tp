package seedu.address.model.event.exceptions;

/**
 * Signals that an operation would result in events with duplicate IDs.
 */
public class DuplicateEventException extends RuntimeException {
    public DuplicateEventException() {
        super("Operation would result in duplicate event IDs");
    }
}
