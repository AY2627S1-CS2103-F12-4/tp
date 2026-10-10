package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapping.Mapper;
import seedu.address.model.mapping.MapperManager;
import seedu.address.model.mapping.Participation;
import seedu.address.model.mapping.ReadOnlyMappings;
import seedu.address.model.mapping.exceptions.DuplicateMappingException;
import seedu.address.model.mapping.exceptions.MappingNotFoundException;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.tag.Tag;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {

    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final AddressBook addressBook;
    private final Mapper mapper;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        this(addressBook, userPrefs, new EventList(), Set.of());
    }

    /**
     * Initializes a ModelManager with the given address book, user preferences,
     * events and participation mappings.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs,
            EventList eventList, Collection<Participation> participations) {
        requireAllNonNull(addressBook, userPrefs, eventList, participations);

        logger.fine(
                "Initializing with address book: "
                        + addressBook
                        + " and user prefs "
                        + userPrefs);

        this.addressBook = new AddressBook(addressBook);
        mapper = new MapperManager(eventList, this.addressBook, participations);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
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
        List<Person> existingPersons = List.copyOf(this.addressBook.getPersonList());
        this.addressBook.resetData(addressBook);
        existingPersons.stream()
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

    //=========== Participation Mappings ====================================================================

    @Override
    public ReadOnlyMappings getMappings() {
        return mapper;
    }

    @Override
    public void addMapping(Event event, Person person) throws DuplicateMappingException {
        mapper.addMapping(event, person);
    }

    @Override
    public void removeMapping(Event event, Person person) throws MappingNotFoundException {
        mapper.removeMapping(event, person);
    }

    @Override
    public Set<Participation> getMappingsForEvent(Event event) throws EventNotFoundException {
        return mapper.getMappingsForEvent(event);
    }

    @Override
    public Set<Participation> getMappingsForPerson(Person person) throws PersonNotFoundException {
        return mapper.getMappingsForPerson(person);
    }

    @Override
    public Participation setPresent(Event event, Person person, boolean isPresent)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException {
        return mapper.setPresent(event, person, isPresent);
    }

    @Override
    public Participation setTags(Event event, Person person, Set<Tag> tags)
            throws MappingNotFoundException, EventNotFoundException, PersonNotFoundException {
        return mapper.setTags(event, person, tags);
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

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && mapper.getMappings().equals(otherModelManager.mapper.getMappings())
                && userPrefs.equals(otherModelManager.userPrefs)
                && filteredPersons.equals(otherModelManager.filteredPersons);
    }
}
