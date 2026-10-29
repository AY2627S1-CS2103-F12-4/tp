package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonEventStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.TypicalPersons;

public class MainAppStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void initializeAndSave_restoresEventsBeforeParticipations() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")),
                new JsonEventStorage(testFolder.resolve("events.json")),
                new JsonParticipationStorage(testFolder.resolve("participations.json")));
        ModelManager original = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Event event = original.createEvent("Workshop", 10, "Java");
        original.addParticipation(event, TypicalPersons.ALICE);
        original.setPresent(event, TypicalPersons.ALICE, true);
        storage.saveAddressBook(original.getAddressBook());
        storage.saveEvents(original.getEvents());
        storage.saveParticipations(original.getParticipations());

        MainApp app = new MainApp();
        Model restored = app.initModelManager(storage, new UserPrefs());
        assertEquals(original.getAddressBook(), restored.getAddressBook());
        assertEquals(List.of(event), restored.getEvents());
        Set<Participation> expected = original.getParticipations().getParticipations();
        assertEquals(expected, restored.getParticipations().getParticipations());
        assertTrue(restored.getParticipationsForEvent(event).iterator().next().isPresent());

        // Saving a command after startup must preserve the restored participation file.
        new LogicManager(restored, storage).execute("list");
        Model restarted = app.initModelManager(storage, new UserPrefs());
        assertEquals(expected, restarted.getParticipations().getParticipations());
        assertEquals("E2", restarted.createEvent("Next", 1, "").getId());
    }

    @Test
    public void initialize_missingEventAndParticipationFiles_preservesPeople() throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        storage.saveAddressBook(TypicalPersons.getTypicalAddressBook());
        Model model = new MainApp().initModelManager(storage, new UserPrefs());
        assertEquals(TypicalPersons.getTypicalAddressBook(), model.getAddressBook());
        assertTrue(model.getEvents().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }
}
