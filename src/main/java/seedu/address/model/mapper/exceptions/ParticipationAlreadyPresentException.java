package seedu.address.model.mapper.exceptions;

/**
 * Signals that a participation is already marked as present.
 */
public class ParticipationAlreadyPresentException extends RuntimeException {

    /**
     * Creates an exception for the participation that is already present.
     */
    public ParticipationAlreadyPresentException(String eventId, String personId) {
        super(String.format("Participation for event %s and person %s is already present", eventId, personId));
    }
}
