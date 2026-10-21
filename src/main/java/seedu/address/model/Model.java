package seedu.address.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.ParticipationAlreadyAbsentException;
import seedu.address.model.mapper.exceptions.ParticipationAlreadyPresentException;
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
     * Adds a participation with event-specific {@code roles} and {@code tags}.
     */
    void addParticipation(EventId eventId, PersonId personId,
                          Set<ParticipationRole> roles, Set<Tag> tags)
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

    /** Edits event-specific fields; null preserves a field and an empty set clears it. */
    void editParticipation(EventId eventId, PersonId personId, Set<ParticipationRole> roles, Set<Tag> tags);

    /**
     * Marks the participation identified by {@code eventId} and {@code personId} as present.
     */
    void markPresent(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, ParticipationAlreadyPresentException,
            EventNotFoundException, PersonNotFoundException;

    /**
     * Marks the participation identified by {@code eventId} and {@code personId} as absent.
     */
    void unmarkPresent(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, ParticipationAlreadyAbsentException,
            EventNotFoundException, PersonNotFoundException;

    /** Returns the participations for {@code event}. */
    Set<Participation> getParticipationsForEvent(Event event) throws EventNotFoundException;

    /**
     * Searches the event identified by {@code eventId} for a nonblank name phrase.
     * Replaces transient search state; invalid input preserves the previous search.
     * Preserves the event filter unless replacing a role search, which restores the ordinary event list.
     * Matches names using case-insensitive substring comparison and preserves distinct person IDs.
     * Results refresh after person and participation changes made through this model.
     *
     * @param eventId Stable ID of the event whose memberships are searched.
     * @param keyword Name phrase, with surrounding whitespace ignored.
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the keyword is blank.
     * @throws EventNotFoundException if no event has the supplied ID.
     */
    void updateParticipantSearch(EventId eventId, String keyword);

    /**
     * Searches roles across all nonarchived events and filters events to those with matching registrations.
     * Matches a case-insensitive substring within a single role, with normalized query whitespace.
     * Each registration appears once even when several of its roles match. Invalid input preserves search state.
     *
     * @param keyword Nonblank role phrase, which need not satisfy assigned-role constraints.
     * @throws NullPointerException if the keyword is null.
     * @throws IllegalArgumentException if the keyword is blank.
     */
    void updateParticipantRoleSearch(String keyword);

    /**
     * Searches roles within the event identified by its stable ID, independently of existing display filters.
     * An archived event produces no results. A nonexistent event or invalid input preserves search state.
     *
     * @param eventId Stable ID of the event whose memberships are searched.
     * @param keyword Nonblank role phrase, with surrounding and repeated internal whitespace ignored.
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the keyword is blank.
     * @throws EventNotFoundException if no event has the supplied ID.
     */
    void updateParticipantRoleSearch(EventId eventId, String keyword);

    /**
     * Returns live, unmodifiable results for the active name or role search.
     * Global role results follow event insertion order, then lexicographic person-ID order within each event.
     * Scoped results are ordered lexicographically by person ID.
     *
     * @return The same observable view across searches and subsequent model changes.
     */
    ObservableList<Participation> getParticipantSearchResults();

    /**
     * Returns an immutable snapshot of matching registrations for the event's stable ID.
     * Read again when the observable search results change to obtain updated rows.
     */
    List<Participation> getParticipantSearchResultsForEvent(Event event);

    /**
     * Returns the scoped search event, or empty during a global role search or when no search is active.
     *
     * @return The event resolved from the selected stable ID.
     */
    Optional<Event> getParticipantSearchEvent();

    /**
     * Returns the search phrase, or empty when no search is active.
     *
     * @return The original case with surrounding whitespace removed; role queries also collapse internal whitespace.
     */
    Optional<String> getParticipantSearchKeyword();

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

    /** Creates an event with an automatically assigned ID. */
    Event createEvent(String name, int capacity, String description);

    /** Updates an existing event, preserving its ID, participations and active filter. */
    void updateEvent(Event event);

    /**
     * Deletes an event and its participations, preserving people and refreshing any active role search.
     * Clears search context if the scoped event is deleted; a role scope also restores the ordinary event list.
     */
    Event deleteEvent(EventId eventId);

    /** Returns a snapshot of all events, including those hidden by the active filter. */
    List<Event> getEvents();

    /** Selects the stored event with this ID, leaving the current selection unchanged on failure. */
    void selectEvent(EventId eventId);

    /** Returns the selected event, or empty if none has been selected in this session. */
    Optional<Event> getSelectedEvent();

    /**
     * Returns a live, unmodifiable view of active events matching the filter, in insertion order.
     */
    ObservableList<Event> getFilteredEventList();

    /**
     * Replaces the event filter with the given non-null predicate. Archived events remain hidden.
     */
    void updateFilteredEventList(Predicate<Event> predicate);

}
