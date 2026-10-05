package seedu.address.model.mapping;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Represents immutable attendance details for one person at one event,
 * including presence and event-specific tags.
 */
public final class Attendance {

    private final Event event;
    private final Person person;
    private final boolean isPresent;
    private final Set<Tag> tags;

    /**
     * Creates an attendance record that is initially not present and has no tags.
     */
    public Attendance(Event event, Person person) {
        this(event, person, false, Set.of());
    }

    /**
     * Creates an attendance record with the given attendance state and
     * event-specific tags.
     */
    public Attendance(Event event, Person person, boolean isPresent, Set<Tag> tags) {
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
     * Returns a copy of this record with the specified attendance state.
     */
    public Attendance withPresent(boolean isPresent) {
        return new Attendance(event, person, isPresent, tags);
    }

    /**
     * Returns a copy of this record with the specified event-specific tags.
     */
    public Attendance withTags(Set<Tag> tags) {
        return new Attendance(event, person, isPresent, tags);
    }

    /**
     * Returns true if both records refer to the same event-person mapping.
     */
    public boolean isSameMapping(Attendance otherAttendance) {
        if (otherAttendance == this) {
            return true;
        }

        return otherAttendance != null
                && Objects.equals(event.getId(), otherAttendance.event.getId())
                && Objects.equals(person.getId(), otherAttendance.person.getId());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Attendance otherAttendance)) {
            return false;
        }

        return isSameMapping(otherAttendance)
                && isPresent == otherAttendance.isPresent
                && tags.equals(otherAttendance.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event.getId(), person.getId(), isPresent, tags);
    }
}
