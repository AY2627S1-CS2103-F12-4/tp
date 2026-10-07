package seedu.address.model.mapping;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Represents immutable participation details for one person at one event,
 * including presence and event-specific tags.
 */
public final class Participation {

    private final Event event;
    private final Person person;
    private final boolean isPresent;
    private final Set<Tag> tags;

    /**
     * Creates a participation record that is initially not present and has no tags.
     */
    public Participation(Event event, Person person) {
        this(event, person, false, Set.of());
    }

    /**
     * Creates a participation record with the given participation state and
     * event-specific tags.
     */
    public Participation(Event event, Person person, boolean isPresent, Set<Tag> tags) {
        requireAllNonNull(event, person, tags);
        this.event = event;
        this.person = person;
        this.isPresent = isPresent;
        this.tags = Set.copyOf(tags);
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

    /**
     * Returns a copy of this record with the specified participation state.
     */
    public Participation withPresent(boolean isPresent) {
        return new Participation(event, person, isPresent, tags);
    }

    /**
     * Returns a copy of this record with the specified event-specific tags.
     */
    public Participation withTags(Set<Tag> tags) {
        return new Participation(event, person, isPresent, tags);
    }

    /**
     * Returns true if both records refer to the same event-person mapping.
     */
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
                && tags.equals(otherParticipation.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event.getId(), person.getId(), isPresent, tags);
    }
}
