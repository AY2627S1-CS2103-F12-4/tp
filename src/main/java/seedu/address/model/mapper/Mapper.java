package seedu.address.model.mapper;

import java.util.Set;

import seedu.address.model.event.Event;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.ParticipationNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Defines operations for managing participations between events and people.
 * Each event-person pair has at most one participation record.
 */
public interface Mapper extends ReadOnlyParticipations {

    /**
     * Adds a participation between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws DuplicateParticipationException if the participation already exists.
     * @throws EventNotFoundException if the event cannot be found.
     * @throws PersonNotFoundException if the person cannot be found.
     */
    void addParticipation(Event event, Person person)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException;

    /**
     * Removes the participation between {@code event} and {@code person}.
     * Both arguments must not be null.
     *
     * @throws ParticipationNotFoundException if the participation does not exist.
     */
    void removeParticipation(Event event, Person person) throws ParticipationNotFoundException;

    /**
     * Returns an unmodifiable snapshot of the participation records for {@code event}.
     * {@code event} must not be null.
     * Participations whose person cannot be found are omitted.
     *
     * @throws EventNotFoundException if a referenced event cannot be found.
     */
    Set<Participation> getParticipationsForEvent(Event event)
            throws EventNotFoundException;

    /**
     * Returns an unmodifiable snapshot of the participation records for
     * {@code person}.
     * {@code person} must not be null.
     * Participations whose event cannot be found are omitted.
     *
     * @throws PersonNotFoundException if a referenced person cannot be found.
     */
    Set<Participation> getParticipationsForPerson(Person person)
            throws PersonNotFoundException;

    /**
     * Sets whether {@code person} is present at {@code event}.
     * Returns the resulting participation record.
     * Both {@code event} and {@code person} must not be null.
     *
     * @throws ParticipationNotFoundException if the participation does not exist.
     * @throws EventNotFoundException if the referenced event cannot be found.
     * @throws PersonNotFoundException if the referenced person cannot be found.
     */
    Participation setPresent(Event event, Person person, boolean isPresent)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException;

    /**
     * Replaces the event-specific tags for {@code person} at {@code event}.
     * Returns the resulting participation record.
     * Arguments must not be null.
     *
     * @throws ParticipationNotFoundException if the participation does not exist.
     * @throws EventNotFoundException if the referenced event cannot be found.
     * @throws PersonNotFoundException if the referenced person cannot be found.
     */
    Participation setTags(Event event, Person person, Set<Tag> tags)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException;

    /**
     * Removes every participation for {@code event}, doing nothing if there are none.
     * {@code event} must not be null.
     */
    void removeParticipationsForEvent(Event event);

    /**
     * Removes every participation for {@code person}, doing nothing if there are none.
     * {@code person} must not be null.
     */
    void removeParticipationsForPerson(Person person);
}
