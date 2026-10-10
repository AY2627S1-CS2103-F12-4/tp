package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
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
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.mapper.exceptions.DuplicateParticipationException;
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
    private final ObservableList<Event> observableEvents;
    private final FilteredList<Event> filteredEvents;
    private final ObservableList<Event> unmodifiableFilteredEvents;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs,
     * an empty event collection, and no participations.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, new EventList(), EMPTY_PARTICIPATIONS);
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
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public PersonId generateNextPersonId() {
        return addressBook.generateNextPersonId();
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        addressBook.setPerson(target, editedPerson);
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
    }

    @Override
    public void addParticipation(EventId eventId, PersonId personId)
            throws DuplicateParticipationException, EventNotFoundException, PersonNotFoundException {
        requireAllNonNull(eventId, personId);
        Event event = eventList.getEventFromId(eventId.toString());
        Person person = addressBook.getPersonFromId(personId);
        if (person == null) {
            throw new PersonNotFoundException();
        }
        mapper.addParticipation(event, person);
    }

    @Override
    public void removeParticipation(Event event, Person person) throws ParticipationNotFoundException {
        mapper.removeParticipation(event, person);
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
    }

    @Override
    public Set<Participation> getParticipationsForEvent(Event event) throws EventNotFoundException {
        return mapper.getParticipationsForEvent(event);
    }

    @Override
    public Set<Participation> getParticipationsForPerson(Person person) throws PersonNotFoundException {
        return mapper.getParticipationsForPerson(person);
    }

    @Override
    public Participation setPresent(Event event, Person person, boolean isPresent)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException {
        return mapper.setPresent(event, person, isPresent);
    }

    @Override
    public Participation setTags(Event event, Person person, Set<Tag> tags)
            throws ParticipationNotFoundException, EventNotFoundException, PersonNotFoundException {
        return mapper.setTags(event, person, tags);
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
                && eventList.getEvents().stream().allMatch(event -> mapper.getParticipationsForEvent(event)
                        .equals(otherModelManager.mapper.getParticipationsForEvent(event)));
    }
}
