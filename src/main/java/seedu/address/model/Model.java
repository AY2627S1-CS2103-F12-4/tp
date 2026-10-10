package seedu.address.model;

import java.util.Set;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.ParticipationNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * The API of the Model component.
 */
public interface Model {

    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /**
     * Returns the AddressBook.
     */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person}
     * exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Generates the next available unique PersonId.
     */
    PersonId generateNextPersonId();

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as
     * another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns all participations. */
    ReadOnlyParticipations getParticipations();

    /**
     * Adds a participation between {@code event} and {@code person}.
     */
    void addParticipation(Event event, Person person)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException;

    /**
     * Adds a participation identified by {@code eventId} and {@code personId}.
     */
    void addParticipation(EventId eventId, PersonId personId)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException;

    /**
     * Removes the participation between {@code event} and {@code person}.
     */
    void removeParticipation(Event event, Person person) throws ParticipationNotFoundException;

    /**
     * Removes a participation identified by {@code eventId} and {@code personId}.
     */
    void removeParticipation(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException;

    /** Returns the participations for {@code event}. */
    Set<Participation> getParticipationsForEvent(Event event) throws EventNotFoundException;

    /** Returns the participations for {@code person}. */
    Set<Participation> getParticipationsForPerson(Person person) throws PersonNotFoundException;

    /**
     * Sets whether {@code person} is present at {@code event}.
     */
    Participation setPresent(Event event, Person person, boolean isPresent)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException;

    /**
     * Replaces the event-specific tags for {@code person} at {@code event}.
     */
    Participation setTags(Event event, Person person, Set<Tag> tags)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException;

    /**
     * Returns an unmodifiable view of the filtered person list.
     */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given
     * {@code predicate}.
     *
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Adds an event with a unique ID to the event collection.
     * Preserves the active event filter.
     */
    void addEvent(Event event);

    /**
     * Returns a live, unmodifiable view of the filtered events in insertion order.
     */
    ObservableList<Event> getFilteredEventList();

    /**
     * Replaces the event filter with the given non-null predicate.
     */
    void updateFilteredEventList(Predicate<Event> predicate);

}
