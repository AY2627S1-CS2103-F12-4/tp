package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Mapper;
import seedu.address.model.mapper.MapperManager;
import seedu.address.model.mapper.ParticipantNameContainsKeywordPredicate;
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
 * Coordinates in-memory people, events, participations, and filtered views.
 * The mapper resolves records from the same collections that this model owns.
 */
public class ModelManager implements Model {

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private static final ReadOnlyParticipations EMPTY_PARTICIPATIONS = Set::of;

    private final AddressBook addressBook;
    private final Mapper mapper;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final EventList eventList;
    private Event selectedEvent;
    private final ObservableList<Event> observableEvents;
    private final FilteredList<Event> filteredEvents;
    private final ObservableList<Event> unmodifiableFilteredEvents;
    private final ObservableList<Participation> participantSearchResults = FXCollections.observableArrayList();
    private final ObservableList<Participation> unmodifiableParticipantSearchResults =
            FXCollections.unmodifiableObservableList(participantSearchResults);
    private EventId participantSearchEventId;
    private String participantSearchKeyword;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs,
     * an empty event collection, and no participations.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, new EventList(), EMPTY_PARTICIPATIONS);
    }

    /** Initializes a model with saved events and no participations. */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs, EventList eventList) {
        this(addressBook, userPrefs, eventList, EMPTY_PARTICIPATIONS);
    }

    /**
     * Initializes a ModelManager with the given address book, user preferences,
     * events and participations.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs,
            EventList eventList, ReadOnlyParticipations participations) {
        requireAllNonNull(addressBook, userPrefs, eventList, participations);

        logger.fine(
                "Initializing with address book: "
                        + addressBook
                        + " and user prefs "
                        + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.eventList = eventList;
        mapper = new MapperManager(this.eventList, this.addressBook, participations.getParticipations());
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        observableEvents = FXCollections.observableArrayList(this.eventList.getEvents());
        filteredEvents = new FilteredList<>(observableEvents);
        unmodifiableFilteredEvents = FXCollections.unmodifiableObservableList(filteredEvents);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    // =========== UserPrefs
    // ==============================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    // =========== AddressBook
    // ============================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        List<Person> previousPersons = List.copyOf(this.addressBook.getPersonList());
        this.addressBook.resetData(addressBook);
        previousPersons.stream()
                .filter(person -> this.addressBook.getPersonFromId(person.getId()) == null)
                .forEach(mapper::removeParticipationsForPerson);
        refreshParticipantSearch();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
        mapper.removeParticipationsForPerson(target);
        refreshParticipantSearch();
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        refreshParticipantSearch();
    }

    @Override
    public PersonId generateNextPersonId() {
        return addressBook.generateNextPersonId();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
        refreshParticipantSearch();
    }

    // =========== Participations
    // ====================================================================

    @Override
    public ReadOnlyParticipations getParticipations() {
        return mapper;
    }

    @Override
    public void addParticipation(Event event, Person person)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException {
        mapper.addParticipation(event, person);
        refreshParticipantSearch();
    }

    @Override
    public void addParticipation(EventId eventId, PersonId personId)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException {
        addParticipation(eventId, personId, Set.of(), Set.of());
    }

    @Override
    public void addParticipation(EventId eventId, PersonId personId,
                                 Set<ParticipationRole> roles, Set<Tag> tags)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException {
        requireAllNonNull(eventId, personId, roles, tags);
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }
        mapper.addParticipation(event, person, roles, tags);
        refreshParticipantSearch();
    }

    @Override
    public void removeParticipation(Event event, Person person) throws ParticipationNotFoundException {
        mapper.removeParticipation(event, person);
        refreshParticipantSearch();
    }

    @Override
    public void removeParticipation(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException {
        requireAllNonNull(eventId, personId);
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }
        mapper.removeParticipation(event, person);
        refreshParticipantSearch();
    }

    @Override
    public void markPresent(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, ParticipationAlreadyPresentException,
            EventNotFoundException, PersonNotFoundException {
        requireAllNonNull(eventId, personId);
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }

        Participation participation = mapper.getParticipationsForEvent(event).stream()
                .filter(candidate -> candidate.getPerson().getId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new ParticipationNotFoundException(eventId.toString(), personId.toString()));
        if (participation.isPresent()) {
            throw new ParticipationAlreadyPresentException(eventId.toString(), personId.toString());
        }
        mapper.setPresent(event, person, true);
        refreshParticipantSearch();
    }

    @Override
    public void unmarkPresent(EventId eventId, PersonId personId)
            throws ParticipationNotFoundException, ParticipationAlreadyAbsentException,
            EventNotFoundException, PersonNotFoundException {
        requireAllNonNull(eventId, personId);
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }

        Participation participation = mapper.getParticipationsForEvent(event).stream()
                .filter(candidate -> candidate.getPerson().getId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new ParticipationNotFoundException(eventId.toString(), personId.toString()));
        if (!participation.isPresent()) {
            throw new ParticipationAlreadyAbsentException(eventId.toString(), personId.toString());
        }
        mapper.setPresent(event, person, false);
        refreshParticipantSearch();
    }

    @Override
    public Set<Participation> getParticipationsForEvent(Event event) throws EventNotFoundException {
        return mapper.getParticipationsForEvent(event);
    }

    @Override
    public void updateParticipantSearch(EventId eventId, String keyword) {
        requireAllNonNull(eventId, keyword);
        ParticipantNameContainsKeywordPredicate predicate = new ParticipantNameContainsKeywordPredicate(keyword);
        Event event = eventList.getEventFromId(eventId.toString());
        List<Participation> matches = getParticipationsForEvent(event).stream()
                .filter(predicate)
                .sorted(Comparator.comparing(participation -> participation.getPerson().getId().toString()))
                .toList();
        participantSearchEventId = eventId;
        participantSearchKeyword = keyword.strip();
        // Publish the context before notifying observers of the new results.
        participantSearchResults.setAll(matches);
    }

    @Override
    public ObservableList<Participation> getParticipantSearchResults() {
        return unmodifiableParticipantSearchResults;
    }

    @Override
    public Optional<Event> getParticipantSearchEvent() {
        return Optional.ofNullable(participantSearchEventId)
                .map(eventId -> eventList.getEventFromId(eventId.toString()));
    }

    @Override
    public Optional<String> getParticipantSearchKeyword() {
        return Optional.ofNullable(participantSearchKeyword);
    }

    private void refreshParticipantSearch() {
        if (participantSearchEventId != null) {
            updateParticipantSearch(participantSearchEventId, participantSearchKeyword);
        }
    }

    @Override
    public Set<Participation> getParticipationsForPerson(Person person) throws PersonNotFoundException {
        return mapper.getParticipationsForPerson(person);
    }

    @Override
    public Participation setPresent(Event event, Person person, boolean isPresent)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException {
        Participation participation = mapper.setPresent(event, person, isPresent);
        refreshParticipantSearch();
        return participation;
    }

    @Override
    public Participation setTags(Event event, Person person, Set<Tag> tags)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException {
        Participation participation = mapper.setTags(event, person, tags);
        refreshParticipantSearch();
        return participation;
    }

    // =========== Filtered Person List Accessors
    // =========================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by
     * the internal list of {@code addressBook}.
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    // =========== Events and Participation
    // ==============================================================

    @Override
    public void addEvent(Event event) {
        eventList.addEvent(event);
        // EventList is the source of truth; refresh the observable view after a
        // successful addition.
        observableEvents.setAll(eventList.getEvents());
    }

    @Override
    public Event createEvent(String name, int capacity, String description) {
        Event event = eventList.createEvent(name, capacity, description);
        observableEvents.setAll(eventList.getEvents());
        return event;
    }

    @Override
    public void updateEvent(Event event) {
        eventList.updateEvent(requireNonNull(event));
        if (selectedEvent != null && selectedEvent.isSameEvent(event)) {
            selectedEvent = event;
        }
        observableEvents.setAll(eventList.getEvents());
        refreshParticipantSearch();
    }

    @Override
    public List<Event> getEvents() {
        return eventList.getEvents();
    }

    @Override
    public void selectEvent(EventId eventId) {
        selectedEvent = eventList.getEventFromId(requireNonNull(eventId).toString());
    }

    @Override
    public Optional<Event> getSelectedEvent() {
        return Optional.ofNullable(selectedEvent);
    }

    @Override
    public Event deleteEvent(EventId eventId) {
        Event event = eventList.getEventFromId(requireNonNull(eventId).toString());
        mapper.removeParticipationsForEvent(event);
        eventList.removeEvent(event.getId());
        if (eventId.equals(participantSearchEventId)) {
            participantSearchEventId = null;
            participantSearchKeyword = null;
            participantSearchResults.clear();
        }
        if (event.equals(selectedEvent)) {
            selectedEvent = null;
        }
        observableEvents.setAll(eventList.getEvents());
        return event;
    }

    @Override
    public ObservableList<Event> getFilteredEventList() {
        return unmodifiableFilteredEvents;
    }

    @Override
    public void updateFilteredEventList(Predicate<Event> predicate) {
        filteredEvents.setPredicate(requireNonNull(predicate));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && mapper.getParticipations().equals(otherModelManager.mapper.getParticipations())
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && eventList.getEvents().equals(otherModelManager.eventList.getEvents())
                && filteredEvents.equals(otherModelManager.filteredEvents)
                && Objects.equals(participantSearchEventId, otherModelManager.participantSearchEventId)
                && Objects.equals(participantSearchKeyword, otherModelManager.participantSearchKeyword)
                && participantSearchResults.equals(otherModelManager.participantSearchResults)
                && eventList.getEvents().stream().allMatch(event -> mapper.getParticipationsForEvent(event)
                        .equals(otherModelManager.mapper.getParticipationsForEvent(event)));
    }
}
