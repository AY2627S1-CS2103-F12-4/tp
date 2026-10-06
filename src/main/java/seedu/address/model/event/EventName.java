package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents an immutable, non-blank event name. Names need not be unique.
 */
public final class EventName {
    public static final String MESSAGE_CONSTRAINTS = "Event names should not be blank";

    private final String value;

    /**
     * Constructs an event name, preserving its spelling and spacing.
     */
    public EventName(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        value = name;
    }

    /**
     * Returns true if the supplied name contains at least one non-whitespace character.
     */
    public static boolean isValidName(String name) {
        requireNonNull(name);
        return !name.isBlank();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EventName otherName && value.equals(otherName.value);
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
