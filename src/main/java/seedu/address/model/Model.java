package seedu.address.model;

import java.util.Set;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.mapping.Participation;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;

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

    /**
     * Adds a participation mapping for an existing event and person.
     * Each event-person pair can be added only once.
     *
     * @throws seedu.address.model.event.exceptions.EventNotFoundException If the event ID is unknown.
     * @throws seedu.address.model.person.exceptions.PersonNotFoundException If the person ID is unknown.
     * @throws seedu.address.model.mapping.exceptions.DuplicateMappingException If the mapping exists.
     */
    void addParticipation(Event event, Person person);

    /**
     * Returns an unmodifiable snapshot of participation records for an existing event.
     * Resolves each record using the stored event and person details.
     */
    Set<Participation> getParticipationsForEvent(Event event);
}
