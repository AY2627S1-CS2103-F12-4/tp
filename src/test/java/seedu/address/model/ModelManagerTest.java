package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.MapperManager;
import seedu.address.model.mapping.Participation;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
        assertTrue(modelManager.getMappings().getMappings().isEmpty());
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void constructor_withMappings_initializesMappings() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        Participation participation = new Participation(event, ALICE);

        MapperManager mappings = new MapperManager(eventList, addressBook, List.of(participation));

        modelManager = new ModelManager(addressBook, new UserPrefs(), eventList, mappings);

        assertEquals(Set.of(participation), modelManager.getMappings().getMappings());
    }

    @Test
    public void addMapping_validEventAndPerson_addsMapping() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        modelManager = new ModelManager(addressBook, new UserPrefs(), eventList,
                new MapperManager(eventList, addressBook));

        modelManager.addMapping(event, ALICE);

        assertEquals(Set.of(new Participation(event, ALICE)), modelManager.getMappings().getMappings());
    }

    @Test
    public void deletePerson_personWithMapping_removesMapping() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mappings = new MapperManager(eventList, addressBook,
                List.of(new Participation(event, ALICE)));
        modelManager = new ModelManager(addressBook, new UserPrefs(), eventList, mappings);

        modelManager.deletePerson(ALICE);

        assertTrue(modelManager.getMappings().getMappings().isEmpty());
    }

    @Test
    public void mappingOperations_existingMapping_updatesAndRemovesMapping() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mappings = new MapperManager(eventList, addressBook,
                List.of(new Participation(event, ALICE)));
        modelManager = new ModelManager(addressBook, new UserPrefs(), eventList, mappings);

        modelManager.setPresent(event, ALICE, true);
        modelManager.setTags(event, ALICE, Set.of());

        Participation updated = new Participation(event, ALICE, true, Set.of(), Set.of());
        assertEquals(Set.of(updated), modelManager.getMappingsForEvent(event));
        assertEquals(Set.of(updated), modelManager.getMappingsForPerson(ALICE));

        modelManager.removeMapping(event, ALICE);
        assertTrue(modelManager.getMappings().getMappings().isEmpty());
    }

    @Test
    public void setAddressBook_personRemoved_allowsMappingToBeAddedAgain() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mappings = new MapperManager(eventList, addressBook,
                List.of(new Participation(event, ALICE)));
        modelManager = new ModelManager(addressBook, new UserPrefs(), eventList, mappings);

        modelManager.setAddressBook(new AddressBook());
        modelManager.addPerson(ALICE);
        modelManager.addMapping(event, ALICE);

        assertEquals(Set.of(new Participation(event, ALICE)), modelManager.getMappings().getMappings());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }

    @Test
    public void equals_differentMappings_returnsFalse() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).build();
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mappings = new MapperManager(eventList, addressBook,
                List.of(new Participation(event, ALICE)));
        ModelManager modelWithMapping = new ModelManager(addressBook, new UserPrefs(), eventList, mappings);
        ModelManager modelWithoutMapping = new ModelManager(addressBook, new UserPrefs(), eventList,
                new MapperManager(eventList, addressBook));

        assertFalse(modelWithMapping.equals(modelWithoutMapping));
    }
}
