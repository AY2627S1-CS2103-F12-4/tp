package seedu.address.model.mapping;

import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapping.exceptions.DuplicateMappingException;
import seedu.address.model.mapping.exceptions.MappingNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Defines operations for managing participation mappings between events and people.
 * Each event-person pair has at most one participation record.
 */
public interface Mapper {

    /**
     * Adds a mapping between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws DuplicateMappingException if the mapping already exists.
     */
    void addMapping(Event event, Person person) throws DuplicateMappingException;

    /**
     * Removes the mapping between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     */
    void removeMapping(Event event, Person person) throws MappingNotFoundException;

    /**
     * Returns an unmodifiable snapshot of the participation records for {@code event}.
     * {@code event} must not be null.
     * Mappings whose person cannot be found are omitted.
     *
     * @throws EventNotFoundException if a referenced event cannot be found.
     */
    Set<Participation> getMappingsForEvent(Event event)
            throws EventNotFoundException;

    /**
     * Returns an unmodifiable snapshot of the participation records for
     * {@code person}.
     * {@code person} must not be null.
     * Mappings whose event cannot be found are omitted.
     *
     * @throws PersonNotFoundException if a referenced person cannot be found.
     */
    Set<Participation> getMappingsForPerson(Person person)
            throws PersonNotFoundException;

    /**
     * Sets whether {@code person} is present at {@code event}.
     * Returns the resulting participation record.
     * Both {@code event} and {@code person} must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     * @throws EventNotFoundException if the referenced event cannot be found.
     * @throws PersonNotFoundException if the referenced person cannot be found.
     */
    Participation setPresent(Event event, Person person, boolean isPresent)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException;

    /**
     * Replaces the event-specific tags for {@code person} at {@code event}.
     * Returns the resulting participation record.
     * Arguments must not be null.
     *
     * @throws MappingNotFoundException if the mapping does not exist.
     * @throws EventNotFoundException if the referenced event cannot be found.
     * @throws PersonNotFoundException if the referenced person cannot be found.
     */
    Participation setTags(Event event, Person person, Set<Tag> tags)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException;

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
