package seedu.address.model.mapping;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Represents a person's participation in an event.
 */
public class Participation {

    private final Event event;
    private final Person person;
    private final boolean isPresent;
    private final Set<Tag> tags;
    private final Set<ParticipationRole> roles;

    /**
     * Creates a participation record that is initially not present,
     * with no tags or roles.
     */
    public Participation(Event event, Person person) {
        this(event, person, false, Set.of(), Set.of());
    }

    /**
     * Creates a participation record with the given state.
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

    public Event getEvent() {
        return event;
    }

    public Person getPerson() {
        return person;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public Set<ParticipationRole> getRoles() {
        return roles;
    }

    public Participation withPresent(boolean isPresent) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    public Participation withTags(Set<Tag> tags) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    public Participation withRoles(Set<ParticipationRole> roles) {
        return new Participation(event, person, isPresent, tags, roles);
    }

    public boolean isSameMapping(Participation otherParticipation) {
        if (otherParticipation == this) {
            return true;
        }

        return otherParticipation != null
                && Objects.equals(event.getId(), otherParticipation.event.getId())
                && Objects.equals(person.getId(), otherParticipation.person.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Participation otherParticipation)) {
            return false;
        }

        return isSameMapping(otherParticipation)
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