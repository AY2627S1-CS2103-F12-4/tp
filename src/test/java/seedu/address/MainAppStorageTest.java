package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;
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

    @Test
    public void initialize_missingAddressBook_backsUpRegistrationsBeforeLoadingSamplePeople() throws Exception {
        StorageManager storage = createStorage();
        ModelManager original = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Event event = original.createEvent("Workshop", 10, "Java");
        original.addParticipation(new EventId(event.getId()), TypicalPersons.ALICE.getId(),
                Set.of(new ParticipationRole("Host")), Set.of(new Tag("VIP")));
        original.setPresent(event, TypicalPersons.ALICE, true);
        storage.saveAddressBook(original.getAddressBook());
        storage.saveEvents(original.getEvents());
        storage.saveParticipations(original.getParticipations());
        String peopleJson = Files.readString(storage.getAddressBookFilePath());
        String eventsJson = Files.readString(storage.getEventFilePath());
        String participationsJson = Files.readString(storage.getParticipationFilePath());
        Files.delete(storage.getAddressBookFilePath());

        MainApp app = new MainApp();
        Model recovered = app.initModelManager(storage, new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), recovered.getAddressBook());
        assertEquals(List.of(event), recovered.getEvents());
        assertTrue(recovered.getParticipations().getParticipations().isEmpty());
        assertFalse(Files.exists(storage.getParticipationFilePath()));
        List<Path> backups = getParticipationBackups();
        assertEquals(1, backups.size());
        Path backup = backups.get(0);
        assertEquals(participationsJson, Files.readString(backup));
        assertEquals(eventsJson, Files.readString(storage.getEventFilePath()));

        new LogicManager(recovered, storage).execute("list");
        Model restarted = app.initModelManager(storage, new UserPrefs());
        assertTrue(restarted.getParticipations().getParticipations().isEmpty());
        assertEquals(backups, getParticipationBackups());
        assertEquals(participationsJson, Files.readString(backup));

        // Restore both matching files: registrations must resolve to the original people again.
        Files.writeString(storage.getAddressBookFilePath(), peopleJson);
        Files.copy(backup, storage.getParticipationFilePath());
        Model restored = app.initModelManager(storage, new UserPrefs());
        assertEquals(original.getAddressBook(), restored.getAddressBook());
        assertEquals(original.getParticipations().getParticipations(),
                restored.getParticipations().getParticipations());
    }

    @Test
    public void initialize_missingEvents_backsUpRegistrationsBeforeEventIdsCanBeReused() throws Exception {
        StorageManager storage = createStorage();
        ModelManager original = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Event event = original.createEvent("Workshop", 10, "Java");
        original.addParticipation(new EventId(event.getId()), TypicalPersons.ALICE.getId(),
                Set.of(new ParticipationRole("Host")), Set.of(new Tag("VIP")));
        original.setPresent(event, TypicalPersons.ALICE, true);
        storage.saveAddressBook(original.getAddressBook());
        storage.saveEvents(original.getEvents());
        storage.saveParticipations(original.getParticipations());
        String peopleJson = Files.readString(storage.getAddressBookFilePath());
        String eventsJson = Files.readString(storage.getEventFilePath());
        String participationsJson = Files.readString(storage.getParticipationFilePath());
        Files.delete(storage.getEventFilePath());

        MainApp app = new MainApp();
        Model recovered = app.initModelManager(storage, new UserPrefs());
        assertEquals(original.getAddressBook(), recovered.getAddressBook());
        assertTrue(recovered.getEvents().isEmpty());
        assertTrue(recovered.getParticipations().getParticipations().isEmpty());

        new LogicManager(recovered, storage).execute("add-event Replacement /cap 5 /desc New event");
        Model restarted = app.initModelManager(storage, new UserPrefs());
        assertEquals("E1", restarted.getEvents().getFirst().getId());
        assertTrue(restarted.getParticipations().getParticipations().isEmpty());
        assertEquals(peopleJson, Files.readString(storage.getAddressBookFilePath()));
        assertFalse(Files.exists(storage.getParticipationFilePath()));
        List<Path> backups = getParticipationBackups();
        assertEquals(1, backups.size());
        Path backup = backups.get(0);
        assertEquals(participationsJson, Files.readString(backup));

        // Restore the original event identities together with their matching registrations.
        Files.writeString(storage.getEventFilePath(), eventsJson);
        Files.copy(backup, storage.getParticipationFilePath());
        Model restored = app.initModelManager(storage, new UserPrefs());
        assertEquals(original.getAddressBook(), restored.getAddressBook());
        assertEquals(List.of(event), restored.getEvents());
        assertEquals(original.getParticipations().getParticipations(),
                restored.getParticipations().getParticipations());
    }

    @Test
    public void initialize_freshFolder_loadsSamplePeopleWithoutBackup() throws Exception {
        Model model = new MainApp().initModelManager(createStorage(), new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), model.getAddressBook());
        assertTrue(model.getEvents().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
        assertTrue(getParticipationBackups().isEmpty());
    }

    @Test
    public void initialize_validEmptyAddressBook_doesNotLoadSamplesOrBackupParticipations() throws Exception {
        StorageManager storage = createStorage();
        storage.saveAddressBook(new AddressBook());
        storage.saveEvents(List.of());
        String participationJson = "{\"participations\":[]}";
        Files.writeString(storage.getParticipationFilePath(), participationJson);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        assertEquals(participationJson, Files.readString(storage.getParticipationFilePath()));
        assertTrue(getParticipationBackups().isEmpty());
    }

    @Test
    public void initialize_validEmptyEvents_doesNotBackupParticipations() throws Exception {
        StorageManager storage = createStorage();
        storage.saveAddressBook(TypicalPersons.getTypicalAddressBook());
        storage.saveEvents(List.of());
        String participationJson = "{\"participations\":[]}";
        Files.writeString(storage.getParticipationFilePath(), participationJson);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(TypicalPersons.getTypicalAddressBook(), model.getAddressBook());
        assertTrue(model.getEvents().isEmpty());
        assertEquals(participationJson, Files.readString(storage.getParticipationFilePath()));
        assertTrue(getParticipationBackups().isEmpty());
    }

    @Test
    public void initialize_bothEntityFilesMissing_createsOnlyOneParticipationBackup() throws Exception {
        StorageManager storage = createStorage();
        String participationJson = """
                {"participations":[{"eventId":"E1","personId":"P1","isPresent":true,
                "roles":["Host"],"tags":["VIP"]}]}
                """;
        Files.writeString(storage.getParticipationFilePath(), participationJson);

        MainApp app = new MainApp();
        Model recovered = app.initModelManager(storage, new UserPrefs());

        assertEquals(SampleDataUtil.getSampleAddressBook(), recovered.getAddressBook());
        assertTrue(recovered.getEvents().isEmpty());
        assertTrue(recovered.getParticipations().getParticipations().isEmpty());
        List<Path> backups = getParticipationBackups();
        assertEquals(1, backups.size());
        assertEquals(participationJson, Files.readString(backups.get(0)));

        app.initModelManager(storage, new UserPrefs());
        assertEquals(backups, getParticipationBackups());
        assertFalse(Files.exists(storage.getParticipationFilePath()));
    }

    @Test
    public void initialize_missingEventsWithInvalidParticipationPath_abortsWithoutChangingPeople() throws Exception {
        StorageManager storage = createStorage();
        storage.saveAddressBook(TypicalPersons.getTypicalAddressBook());
        String peopleJson = Files.readString(storage.getAddressBookFilePath());
        Files.createDirectory(storage.getParticipationFilePath());
        Path existingData = storage.getParticipationFilePath().resolve("keep.txt");
        Files.writeString(existingData, "preserve this data");

        assertThrows(IOException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));

        assertEquals(peopleJson, Files.readString(storage.getAddressBookFilePath()));
        assertEquals("preserve this data", Files.readString(existingData));
        assertFalse(Files.exists(storage.getEventFilePath()));
        assertTrue(getParticipationBackups().isEmpty());
    }

    @Test
    public void initialize_missingAddressBook_backsUpMalformedParticipationsWithoutReplacingEarlierBackup()
            throws Exception {
        StorageManager storage = createStorage();
        Path earlierBackup = testFolder.resolve("participations.orphaned-previous.json");
        Files.writeString(earlierBackup, "earlier backup");
        String malformedJson = "{unreadable participation data";
        Files.writeString(storage.getParticipationFilePath(), malformedJson);

        Model model = new MainApp().initModelManager(storage, new UserPrefs());

        assertTrue(model.getParticipations().getParticipations().isEmpty());
        assertFalse(Files.exists(storage.getParticipationFilePath()));
        List<Path> backups = getParticipationBackups();
        assertEquals(2, backups.size());
        Path newBackup = backups.stream().filter(path -> !path.equals(earlierBackup)).findFirst().orElseThrow();
        assertEquals(malformedJson, Files.readString(newBackup));
        assertEquals("earlier backup", Files.readString(earlierBackup));
    }

    @Test
    public void initialize_invalidParticipationPath_abortsRecoveryWithoutMovingData() throws Exception {
        StorageManager storage = createStorage();
        Files.createDirectory(storage.getParticipationFilePath());
        Path existingData = storage.getParticipationFilePath().resolve("keep.txt");
        Files.writeString(existingData, "preserve this data");

        assertThrows(IOException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));

        assertEquals("preserve this data", Files.readString(existingData));
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
        assertTrue(getParticipationBackups().isEmpty());
    }

    @Test
    public void initialize_nullParticipationRecord_recoversWithEmptyParticipations() throws Exception {
        assertParticipationRecovery("""
                {"participations":[null]}
                """);
    }

    @Test
    public void initialize_nullParticipationTag_recoversWithEmptyParticipations() throws Exception {
        assertParticipationRecovery("""
                {"participations":[{"eventId":"E1","personId":"P1","tags":[null]}]}
                """);
    }

    /** Returns storage configured entirely within this test's temporary folder. */
    private StorageManager createStorage() {
        return new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")),
                new JsonEventStorage(testFolder.resolve("events.json")),
                new JsonParticipationStorage(testFolder.resolve("participations.json")));
    }

    /** Returns participation backups in this test's data folder. */
    private List<Path> getParticipationBackups() throws IOException {
        try (Stream<Path> paths = Files.list(testFolder)) {
            return paths.filter(path -> path.getFileName().toString().startsWith("participations.orphaned-"))
                    .toList();
        }
    }

    /**
     * Verifies that invalid participation data preserves people and events during startup recovery.
     */
    private void assertParticipationRecovery(String json) throws Exception {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")),
                new JsonEventStorage(testFolder.resolve("events.json")),
                new JsonParticipationStorage(testFolder.resolve("participations.json")));
        ModelManager original = new ModelManager(TypicalPersons.getTypicalAddressBook(), new UserPrefs());
        Event event = original.createEvent("Workshop", 10, "Java");
        original.addParticipation(event, TypicalPersons.ALICE);
        storage.saveAddressBook(original.getAddressBook());
        storage.saveEvents(original.getEvents());
        Files.writeString(storage.getParticipationFilePath(), json);

        Model restored = new MainApp().initModelManager(storage, new UserPrefs());

        assertEquals(original.getAddressBook(), restored.getAddressBook());
        assertEquals(List.of(event), restored.getEvents());
        assertTrue(restored.getParticipations().getParticipations().isEmpty());
        assertEquals(json, Files.readString(storage.getParticipationFilePath()));
    }
}
