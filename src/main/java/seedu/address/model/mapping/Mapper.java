package seedu.address.model.mapping;

import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * The API for managing mappings between events and their attendees.
 */
public interface Mapper {

    /**
     * Adds a mapping between {@code event} and {@code person}.
     */
    void addMapping(Event event, Person person);

    /**
     * Removes the mapping between {@code event} and {@code person}.
     */
    void removeMapping(Event event, Person person);

    /**
     * Returns an unmodifiable snapshot of the attendance records for {@code event}.
     */
    Set<Attendance> getMappingsForEvent(Event event);

    /**
     * Returns an unmodifiable snapshot of the attendance records for
     * {@code person}.
     */
    Set<Attendance> getMappingsForPerson(Person person);

    /**
     * Sets whether {@code person} is present at {@code event}.
     * Returns the resulting attendance record.
     */
    Attendance setPresent(Event event, Person person, boolean isPresent);

    /**
     * Replaces the event-specific tags for {@code person} at {@code event}.
     * Returns the resulting attendance record.
     */
    Attendance setTags(Event event, Person person, Set<Tag> tags);

    /**
     * Removes every mapping for {@code event}.
     */
    void removeMappingsForEvent(Event event);

    /**
     * Removes every mapping for {@code person}.
     */
    void removeMappingsForPerson(Person person);
}
