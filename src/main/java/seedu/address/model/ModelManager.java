package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Mapper;
import seedu.address.model.mapper.MapperManager;
import seedu.address.model.mapper.ParticipantNameContainsKeywordPredicate;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.ParticipationRoleContainsKeywordPredicate;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
import seedu.address.model.mapper.exceptions.EventAtCapacityException;
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

    private enum ParticipantSearchType {
        NAME,
        ROLE
    }

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
    private ParticipantSearchType participantSearchType;
    private boolean participantSearchIncludesArchived;

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
        filteredEvents = new FilteredList<>(observableEvents, event -> !event.isArchived());
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
            throws DuplicateParticipationException, EventAtCapacityException,
            EventNotFoundException, PersonNotFoundException {
        mapper.addParticipation(event, person);
        refreshParticipantSearch();
    }

    @Override
    public void addParticipation(EventId eventId, PersonId personId)
            throws DuplicateParticipationException, EventAtCapacityException,
            EventNotFoundException, PersonNotFoundException {
        addParticipation(eventId, personId, Set.of(), Set.of());
    }

    @Override
    public void addParticipation(EventId eventId, PersonId personId,
                                 Set<ParticipationRole> roles, Set<Tag> tags)
            throws DuplicateParticipationException, EventAtCapacityException,
            EventNotFoundException, PersonNotFoundException {
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
    public void editParticipation(EventId eventId, PersonId personId, Set<ParticipationRole> roles, Set<Tag> tags) {
        requireAllNonNull(eventId, personId);
        if (roles == null && tags == null) {
            throw new IllegalArgumentException("At least one role or tag field must be provided.");
        }
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }
        Participation current = mapper.getParticipationsForEvent(event).stream()
                .filter(candidate -> candidate.getPerson().getId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new ParticipationNotFoundException(eventId.toString(), personId.toString()));
        mapper.editDetails(event, person, roles == null ? current.getRoles() : roles,
                tags == null ? current.getTags() : tags);
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
        updateParticipantSearch(eventId, keyword, false);
    }

    @Override
    public void updateParticipantSearch(EventId eventId, String keyword, boolean includeArchived) {
        applyParticipantSearch(eventId, keyword, ParticipantSearchType.NAME, includeArchived);
    }

    @Override
    public void updateParticipantSearch(String keyword) {
        updateParticipantSearch(keyword, false);
    }

    @Override
    public void updateParticipantSearch(String keyword, boolean includeArchived) {
        applyParticipantSearch(eventList.getEvents(), null, keyword, ParticipantSearchType.NAME, includeArchived);
    }

    @Override
    public void updateParticipantRoleSearch(EventId eventId, String keyword) {
        updateParticipantRoleSearch(eventId, keyword, false);
    }

    @Override
    public void updateParticipantRoleSearch(EventId eventId, String keyword, boolean includeArchived) {
        applyParticipantSearch(eventId, keyword, ParticipantSearchType.ROLE, includeArchived);
    }

    @Override
    public void updateParticipantRoleSearch(String keyword) {
        updateParticipantRoleSearch(keyword, false);
    }

    @Override
    public void updateParticipantRoleSearch(String keyword, boolean includeArchived) {
        applyParticipantSearch(eventList.getEvents(), null, keyword, ParticipantSearchType.ROLE, includeArchived);
    }

    private void applyParticipantSearch(EventId eventId, String keyword, ParticipantSearchType searchType,
            boolean includeArchived) {
        requireAllNonNull(eventId, keyword);
        // Resolve archived scopes too, so nonexistent IDs fail before changing the previous search.
        Event event = eventList.getEventFromId(eventId.toString());
        applyParticipantSearch(List.of(event), eventId, keyword, searchType, includeArchived);
    }

    private void applyParticipantSearch(List<Event> events, EventId eventId, String keyword,
            ParticipantSearchType searchType, boolean includeArchived) {
        String normalizedKeyword = StringUtil.normalizeWhitespace(keyword);
        Predicate<Participation> predicate = switch (searchType) {
            case NAME -> new ParticipantNameContainsKeywordPredicate(normalizedKeyword);
            case ROLE -> new ParticipationRoleContainsKeywordPredicate(normalizedKeyword);
        };
        List<Participation> matches = events.stream()
                .filter(event -> includeArchived || !event.isArchived())
                .flatMap(event -> getParticipationsForEvent(event).stream()
                        .filter(predicate)
                        .sorted(Comparator.comparing(participation -> participation.getPerson().getId())))
                .toList();
        Set<String> matchingEventIds = matches.stream()
                .map(participation -> participation.getEvent().getId()).collect(Collectors.toSet());

        participantSearchType = searchType;
        participantSearchEventId = eventId;
        participantSearchKeyword = normalizedKeyword;
        participantSearchIncludesArchived = includeArchived;
        // Publish the search context and event filter before notifying result observers.
        updateFilteredEventList(event -> matchingEventIds.contains(event.getId()), includeArchived);
        participantSearchResults.setAll(matches);
    }

    @Override
    public void clearParticipantSearch() {
        showEvents(false);
    }

    @Override
    public void showEvents(boolean includeArchived) {
        participantSearchType = null;
        participantSearchEventId = null;
        participantSearchKeyword = null;
        participantSearchIncludesArchived = false;
        updateFilteredEventList(event -> true, includeArchived);
        participantSearchResults.clear();
    }

    @Override
    public ObservableList<Participation> getParticipantSearchResults() {
        return unmodifiableParticipantSearchResults;
    }

    @Override
    public List<Participation> getParticipantSearchResultsForEvent(Event event) {
        requireNonNull(event);
        return participantSearchResults.stream()
                .filter(participation -> participation.getEvent().isSameEvent(event)).toList();
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

    @Override
    public boolean isParticipantSearchIncludingArchived() {
        return participantSearchIncludesArchived;
    }

    private void refreshParticipantSearch() {
        if (participantSearchType == null) {
            return;
        }
        if (participantSearchEventId == null) {
            applyParticipantSearch(eventList.getEvents(), null, participantSearchKeyword, participantSearchType,
                    participantSearchIncludesArchived);
        } else {
            applyParticipantSearch(participantSearchEventId, participantSearchKeyword, participantSearchType,
                    participantSearchIncludesArchived);
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
        refreshParticipantSearch();
    }

    @Override
    public Event createEvent(String name, int capacity, String description) {
        Event event = eventList.createEvent(name, capacity, description);
        observableEvents.setAll(eventList.getEvents());
        refreshParticipantSearch();
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
    public BigInteger getNextEventId() {
        return eventList.getNextId();
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
            clearParticipantSearch();
        }
        if (event.equals(selectedEvent)) {
            selectedEvent = null;
        }
        observableEvents.setAll(eventList.getEvents());
        refreshParticipantSearch();
        return event;
    }

    @Override
    public ObservableList<Event> getFilteredEventList() {
        return unmodifiableFilteredEvents;
    }

    @Override
    public void updateFilteredEventList(Predicate<Event> predicate) {
        updateFilteredEventList(predicate, false);
    }

    @Override
    public void updateFilteredEventList(Predicate<Event> predicate, boolean includeArchived) {
        requireNonNull(predicate);
        filteredEvents.setPredicate(event -> (includeArchived || !event.isArchived()) && predicate.test(event));
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
                && participantSearchType == otherModelManager.participantSearchType
                && participantSearchIncludesArchived == otherModelManager.participantSearchIncludesArchived
                && participantSearchResults.equals(otherModelManager.participantSearchResults)
                && eventList.getEvents().stream().allMatch(event -> mapper.getParticipationsForEvent(event)
                        .equals(otherModelManager.mapper.getParticipationsForEvent(event)));
    }
}
