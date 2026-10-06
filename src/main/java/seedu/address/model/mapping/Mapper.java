package seedu.address.model.mapping;

import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.mapping.exceptions.DuplicateMappingException;
import seedu.address.model.mapping.exceptions.MappingNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Defines operations for managing attendance mappings between events and people.
 * Each event-person pair has at most one attendance record.
 */
public interface Mapper {

    /**
     * Adds a mapping between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws DuplicateMappingException if the mapping already exists.
     */
    void addMapping(Event event, Person person);

    /**
     * Removes the mapping between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     */
    void removeMapping(Event event, Person person);

    /**
     * Returns an unmodifiable snapshot of the attendance records for {@code event}.
     * {@code event} must not be null.
     */
    Set<Attendance> getMappingsForEvent(Event event);

    /**
     * Returns an unmodifiable snapshot of the attendance records for
     * {@code person}.
     * {@code person} must not be null.
     */
    Set<Attendance> getMappingsForPerson(Person person);

    /**
     * Sets whether {@code person} is present at {@code event}.
     * Returns the resulting attendance record.
     * Both {@code event} and {@code person} must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     */
    Attendance setPresent(Event event, Person person, boolean isPresent);

    /**
     * Replaces the event-specific tags for {@code person} at {@code event}.
     * Returns the resulting attendance record.
     * Arguments must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     */
    Attendance setTags(Event event, Person person, Set<Tag> tags);

    /**
     * Removes every mapping for {@code event}, doing nothing if there are none.
     * {@code event} must not be null.
     */
    void removeMappingsForEvent(Event event);

    /**
     * Removes every mapping for {@code person}, doing nothing if there are none.
     * {@code person} must not be null.
     */
    void removeMappingsForPerson(Person person);
}
