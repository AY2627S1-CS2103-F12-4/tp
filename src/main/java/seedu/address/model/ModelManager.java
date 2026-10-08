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
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.Mapper;
import seedu.address.model.mapping.MapperManager;
import seedu.address.model.mapping.Participation;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * Coordinates in-memory people, events, participation mappings, and filtered views.
 * The mapper resolves records from the same collections that this model owns.
 */
public class ModelManager implements Model {

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final EventList eventList;
    private final Mapper mapper;
    private final ObservableList<Event> observableEvents;
    private final FilteredList<Event> filteredEvents;
    private final ObservableList<Event> unmodifiableFilteredEvents;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs,
     * an empty event collection, and no participation mappings.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine(
                "Initializing with address book: "
                        + addressBook
                        + " and user prefs "
                        + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        eventList = new EventList();
        mapper = new MapperManager(eventList, this.addressBook);
        observableEvents = FXCollections.observableArrayList();
        filteredEvents = new FilteredList<>(observableEvents);
        unmodifiableFilteredEvents = FXCollections.unmodifiableObservableList(filteredEvents);
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==============================================================================

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

    //=========== AddressBook ============================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        List<Person> previousPersons = List.copyOf(this.addressBook.getPersonList());
        this.addressBook.resetData(addressBook);
        previousPersons.stream()
                .filter(person -> this.addressBook.getPersonFromId(person.getId()) == null)
                .forEach(mapper::removeMappingsForPerson);
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
        mapper.removeMappingsForPerson(target);
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

    //=========== Filtered Person List Accessors =========================================================

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

    //=========== Events and Participation ==============================================================

    @Override
    public void addEvent(Event event) {
        eventList.addEvent(event);
        // EventList is the source of truth; refresh the observable view after a successful addition.
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
    public void addParticipation(Event event, Person person) {
        requireAllNonNull(event, person);
        Event storedEvent = eventList.getEventFromId(event.getId());
        Person storedPerson = addressBook.getPersonFromId(person.getId());
        if (storedPerson == null) {
            throw new PersonNotFoundException();
        }
        mapper.addMapping(storedEvent, storedPerson);
    }

    @Override
    public Set<Participation> getParticipationsForEvent(Event event) {
        return mapper.getMappingsForEvent(event);
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
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons)
                && eventList.getEvents().equals(otherModelManager.eventList.getEvents())
                && filteredEvents.equals(otherModelManager.filteredEvents)
                && eventList.getEvents().stream().allMatch(event -> mapper.getMappingsForEvent(event)
                        .equals(otherModelManager.mapper.getMappingsForEvent(event)));
    }
}
