package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an Event's unique ID.
 */
public final class EventId {
    public static final String MESSAGE_CONSTRAINTS =
            "Event IDs should start with E followed by one or more digits.";

    private static final String VALIDATION_REGEX = "E\\d+";

    public final String value;

    /**
     * Constructs an {@code EventId}.
     *
     * @param id A valid event ID.
     */
    public EventId(String id) {
        requireNonNull(id);
        checkArgument(isValidEventId(id), MESSAGE_CONSTRAINTS);
        value = id;
    }

    /**
     * Returns true if a given string is a valid event ID.
     */
    public static boolean isValidEventId(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EventId otherId && value.equals(otherId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
