package seedu.address.model.mapper.exceptions;

/**
 * Signals that a participation is already marked as absent.
 */
public class ParticipationAlreadyAbsentException extends RuntimeException {

    /**
     * Creates an exception for the participation that is already absent.
     */
    public ParticipationAlreadyAbsentException(String eventId, String personId) {
        super(String.format("Participation for event %s and person %s is already absent", eventId, personId));
    }
}
