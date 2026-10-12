package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

public class StorageManagerTest {

    @TempDir
    public Path testFolder;

    private StorageManager storageManager;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(getTempFilePath("ab"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(getTempFilePath("prefs"));
        storageManager = new StorageManager(addressBookStorage, userPrefsStorage);
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
    public void getEventFilePath_defaultStorage_usesSiblingEventsFile() {
        assertEquals(getTempFilePath("events.json"), storageManager.getEventFilePath());
    }

    @Test
    public void eventsReadSave_customStorage_preservesEventsAndPersonFile() throws Exception {
        Path eventPath = getTempFilePath("custom-events.json");
        storageManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new JsonEventStorage(eventPath));
        assertEquals(eventPath, storageManager.getEventFilePath());
        AddressBook people = getTypicalAddressBook();
        storageManager.saveAddressBook(people);
        String personFile = Files.readString(getTempFilePath("ab"));
        List<Event> events = List.of(new Event(new EventId("E7"), "Workshop", 20, "Java"));
        storageManager.saveEvents(events);
        assertEquals(events, storageManager.readEvents().orElseThrow().getEvents());
        assertEquals(personFile, Files.readString(getTempFilePath("ab")));

        storageManager.saveEvents(List.of());
        assertEquals(List.of(), storageManager.readEvents().orElseThrow().getEvents());
    }

    @Test
    public void readEvents_missingFile_returnsEmpty() throws Exception {
        assertFalse(storageManager.readEvents().isPresent());
    }

    @Test
    public void readEvents_invalidFile_propagatesDataLoadingException() throws Exception {
        Files.writeString(storageManager.getEventFilePath(), "invalid JSON");
        assertThrows(DataLoadingException.class, storageManager::readEvents);
    }

    @Test
    public void saveEvents_unwritableTarget_propagatesIoException() throws Exception {
        Path directory = Files.createDirectory(getTempFilePath("event-directory"));
        storageManager = new StorageManager(new JsonAddressBookStorage(getTempFilePath("ab")),
                new JsonUserPrefsStorage(getTempFilePath("prefs")), new JsonEventStorage(directory));
        assertThrows(IOException.class, () -> storageManager.saveEvents(List.of()));
    }

}
