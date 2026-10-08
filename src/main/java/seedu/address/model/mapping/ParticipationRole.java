package seedu.address.model.mapping;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a participant's role in an event.
 * Guarantees: immutable; is valid as declared in {@link #isValidRole(String)}
 */
public class ParticipationRole {

    public static final String MESSAGE_CONSTRAINTS =
            "Roles should be 1 to 50 characters and may contain letters, numbers, spaces, "
                    + "apostrophes, hyphens and parentheses.";

    private static final String VALIDATION_REGEX = "[A-Za-z0-9'()\\- ]{1,50}";

    public final String value;

    /**
     * Constructs a {@code ParticipationRole}.
     *
     * @param role A valid role.
     */
    public ParticipationRole(String role) {
        requireNonNull(role);

        String normalizedRole = role.trim().replaceAll("\\s+", " ");

        checkArgument(isValidRole(normalizedRole), MESSAGE_CONSTRAINTS);
        value = normalizedRole;
    }

    /**
     * Returns true if a given string is a valid role.
     */
    public static boolean isValidRole(String test) {
        return test != null
                && !test.isBlank()
                && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ParticipationRole
                        && value.equals(((ParticipationRole) other).value));
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
