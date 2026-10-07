package seedu.address.model.participant;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Participant's unique ID.
 */
public class ParticipantId {

    public static final String MESSAGE_CONSTRAINTS =
            "Participant IDs should start with P followed by one or more digits.";

    private static final String VALIDATION_REGEX = "P\\d+";

    public final String value;

    /**
     * Constructs a {@code ParticipantId}.
     *
     * @param id A valid participant ID.
     */
    public ParticipantId(String id) {
        requireNonNull(id);
        checkArgument(isValidParticipantId(id), MESSAGE_CONSTRAINTS);
        value = id;
    }

    /**
     * Returns true if a given string is a valid participant ID.
     */
    public static boolean isValidParticipantId(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ParticipantId
                && value.equals(((ParticipantId) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}