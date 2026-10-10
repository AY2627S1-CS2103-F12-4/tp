package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.MapperManager;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        JsonParticipationStorage participationStorage = new JsonParticipationStorage(getTempFilePath("participations"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage, participationStorage);
    }

    private Path getTempFilePath(String fileName) {
        return testFolder.resolve(fileName);
    }

    @Test
    public void prefsReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonUserPrefsStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonUserPrefsStorageTest} class.
         */
        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(300, 600, 4, 6));
        storageManager.saveUserPrefs(original);
        UserPrefs retrieved = storageManager.readUserPrefs().get();
        assertEquals(original, retrieved);
    }

    @Test
    public void addressBookReadSave() throws Exception {
        /*
         * Note: This is an integration test that verifies the StorageManager is properly wired to the
         * {@link JsonAddressBookStorage} class.
         * More extensive testing of UserPref saving/reading is done in {@link JsonAddressBookStorageTest} class.
         */
        AddressBook original = getTypicalAddressBook();
        storageManager.saveAddressBook(original);
        ReadOnlyAddressBook retrieved = storageManager.readAddressBook().get();
        assertEquals(original, new AddressBook(retrieved));
    }

    @Test
    public void getAddressBookFilePath() {
        assertNotNull(storageManager.getAddressBookFilePath());
    }

    @Test
    public void participationsReadSave() throws Exception {
        Person person = new PersonBuilder().withId("P1").build();
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(person);
        Event event = new Event(new EventId("E1"), "Orientation", 100, "Welcome event");
        EventList eventList = new EventList();
        eventList.addEvent(event);
        MapperManager mapper = new MapperManager(eventList, addressBook);
        mapper.addParticipation(event, person);

        storageManager.saveParticipations(mapper);
        ReadOnlyParticipations retrieved = storageManager.readParticipations(eventList, addressBook).orElseThrow();

        assertEquals(mapper.getParticipations(), retrieved.getParticipations());
    }

    @Test
    public void getParticipationFilePath() {
        assertNotNull(storageManager.getParticipationFilePath());
    }

}
