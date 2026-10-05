package seedu.address.model.participant;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.model.person.Name;

/**
 * Represents a participant in AttendPlusPlus.
 * Guarantees: details are present and not null.
 */
public class Participant {

    private final ParticipantId id;
    private final Name name;
    private final Role role;

    /**
     * Creates a Participant with the given ID, name and role.
     */
    public Participant(ParticipantId id, Name name, Role role) {
        requireAllNonNull(id, name, role);
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public ParticipantId getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Participant)) {
            return false;
        }

        Participant otherParticipant = (Participant) other;

        return id.equals(otherParticipant.id)
                && name.equals(otherParticipant.name)
                && role.equals(otherParticipant.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, role);
    }

    @Override
    public String toString() {
        return id + " — " + name + " — " + role;
    }
}