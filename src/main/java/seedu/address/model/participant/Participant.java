package seedu.address.model.participant;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.model.person.Person;

/**
 * Represents a participant in AttendPlusPlus.
 * A participant contains a Person together with event-specific participant details.
 * Guarantees: details are present and not null.
 */
public class Participant {

    private final ParticipantId id;
    private final Person person;
    private final Role role;

    /**
     * Creates a Participant with the given ID, person and role.
     */
    public Participant(ParticipantId id, Person person, Role role) {
        requireAllNonNull(id, person, role);
        this.id = id;
        this.person = person;
        this.role = role;
    }

    public ParticipantId getId() {
        return id;
    }

    public Person getPerson() {
        return person;
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
                && person.equals(otherParticipant.person)
                && role.equals(otherParticipant.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, person, role);
    }

    @Override
    public String toString() {
        return id + " — " + person.getName() + " — " + role;
    }
}