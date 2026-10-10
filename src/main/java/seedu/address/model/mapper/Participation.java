package seedu.address.model.mapper;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Represents immutable participation details for one person at one event,
 * including presence, event-specific tags and participation roles.
 */
public final class Participation {

    private final Event event;
    private final Person person;
    private final boolean isPresent;
    private final Set<Tag> tags;
    private final Set<ParticipationRole> roles;

    /**
     * Creates a participation record that is initially not present
     * and has no tags or roles.
     */
    public Participation(Event event, Person person) {
        this(event, person, false, Set.of(), Set.of());
    }

    /**
     * Creates a participation record with the given participation state,
     * event-specific tags and roles.
     */
    public Participation(Event event, Person person, boolean isPresent,
                         Set<Tag> tags, Set<ParticipationRole> roles) {
        requireAllNonNull(event, person, tags, roles);

        this.event = event;
        this.person = person;
        this.isPresent = isPresent;
        this.tags = Set.copyOf(tags);
        this.roles = Set.copyOf(roles);
    }

    /**
     * Returns the event associated with this participation.
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the person associated with this participation.
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Returns true if the person is present for the event.
     */
    public boolean isPresent() {
        return isPresent;
    }

    /**
     * Returns the event-specific tags associated with this participation.
     */
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Returns the roles associated with this participation.
     */
    public Set<ParticipationRole> getRoles() {
        return roles;
    }

    /**
     * Returns a copy of this record with the specified participation state.
     */
    public Participation withPresent(boolean isPresent) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    /**
     * Returns a copy of this record with the specified event-specific tags.
     */
    public Participation withTags(Set<Tag> tags) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    /**
     * Returns a copy of this record with the specified participation roles.
     */
    public Participation withRoles(Set<ParticipationRole> roles) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    /**
     * Returns true if both records refer to the same event-person participation.
     */
    public boolean isSameParticipation(Participation otherParticipation) {
        if (otherParticipation == this) {
            return true;
        }

        return otherParticipation != null
                && Objects.equals(
                event.getId(),
                otherParticipation.event.getId())
                && Objects.equals(
                person.getId(),
                otherParticipation.person.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Participation otherParticipation)) {
            return false;
        }

        return isSameParticipation(otherParticipation)
                && isPresent == otherParticipation.isPresent
                && tags.equals(otherParticipation.tags)
                && roles.equals(otherParticipation.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                event.getId(),
                person.getId(),
                isPresent,
                tags,
                roles);
    }
}
