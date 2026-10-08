package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's unique ID.
 * Guarantees: immutable; is valid as declared in {@link #isValidPersonId(String)}
 */
public class PersonId {

    public static final String MESSAGE_CONSTRAINTS =
            "Person IDs should start with P followed by one or more digits.";

    private static final String VALIDATION_REGEX = "P\\d+";

    public final String value;

    /**
     * Constructs a {@code PersonId}.
     *
     * @param id A valid person ID.
     */
    public PersonId(String id) {
        requireNonNull(id);
        checkArgument(isValidPersonId(id), MESSAGE_CONSTRAINTS);
        value = id;
    }

    /**
     * Returns true if a given string is a valid person ID.
     */
    public static boolean isValidPersonId(String test) {
        return test != null && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof PersonId
                && value.equals(((PersonId) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
