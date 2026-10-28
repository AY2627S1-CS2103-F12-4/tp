package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteParticipantCommand;
import seedu.address.logic.commands.MarkParticipantCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.mapper.ReadOnlyParticipations;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ParticipantCommandIntegrationTest {

    private static final EventId EVENT_ID = new EventId("E1");
    private static final PersonId PERSON_ID = new PersonId("P1");
    private static final Event EVENT = new Event(EVENT_ID, "Workshop", 10, "Learn Java");
    private static final Person PERSON = new PersonBuilder().withId("P1").build();

    @TempDir
    public Path temporaryFolder;

    private AddressBook addressBook;
    private EventList eventList;
    private Logic logic;
    private Model model;
    private ParticipationStorageStub participationStorage;

    @Test
    public void execute_mistypedOptions_rejectsWithoutChangingOrPersistingData() throws Exception {
        logic.execute("add-participant E1 P1 /role Host /tag VIP");
        var registrations = Set.copyOf(model.getParticipations().getParticipations());
        var stored = participationStorage.readParticipations(eventList, addressBook).orElseThrow();
        for (String command : new String[] {
            "edit-participant E1 P1 /roles Speaker",
            "edit-participant E1 P1 /tags",
            "edit-participant E1 P1 /role Speaker /tags Guest",
            "edit-participant E1 P1 /roleSpeaker",
            "edit-event E1 /description Updated venue",
            "edit-event E1 Renamed /desc Venue /description Updated",
            "edit-event E1 /capacity 20",
            "edit-event E1 /cap20",
            "add-participant E1 P1 /roles Speaker",
            "add-participant E1 P1 /tags VIP",
            "add-event Party /capacity 20 /desc Venue",
            "add-event Party /cap 20 /description Venue"}) {
            assertThrows(ParseException.class, () -> logic.execute(command));
            assertEquals(java.util.List.of(EVENT), model.getEvents());
            assertEquals(registrations, model.getParticipations().getParticipations());
            assertEquals(stored.getParticipations(), participationStorage.readParticipations(eventList, addressBook)
                    .orElseThrow().getParticipations());
        }
    }

    @BeforeEach
    public void setUp() {
        addressBook = new AddressBook();
        addressBook.addPerson(PERSON);
        eventList = new EventList();
        eventList.addEvent(EVENT);
        model = new ModelManager(addressBook, new UserPrefs(), eventList, Set::of);

        participationStorage = new ParticipationStorageStub(temporaryFolder.resolve("participations.json"));
        logic = createLogic();
    }

    @Test
    public void execute_unchangedParticipations_allowsHelpAndExitWithReadOnlyStorage() throws Exception {
        participationStorage.saveFailure = new AccessDeniedException("participations.json");

        logic.execute("list-event");
        assertTrue(logic.execute("help").shouldShowHelp());
        assertTrue(logic.execute("exit").shouldExit());
        assertEquals(0, participationStorage.saveAttempts);
    }

    @Test
    public void execute_loadedParticipations_allowsHelpAndExitWithReadOnlyStorage() throws Exception {
        model.addParticipation(EVENT_ID, PERSON_ID);
        participationStorage.saveParticipations(model.getParticipations());
        logic = createLogic();
        participationStorage.saveFailure = new AccessDeniedException("participations.json");

        assertTrue(logic.execute("help").shouldShowHelp());
        assertTrue(logic.execute("exit").shouldExit());
        assertEquals(1, participationStorage.saveAttempts);
        assertFalse(getOnlyStoredParticipation().isPresent());
    }

    @Test
    public void execute_unchangedParticipationDetails_doesNotSave() throws Exception {
        logic.execute("add-participant E1 P1 /role Host /tag VIP");
        participationStorage.saveFailure = new AccessDeniedException("participations.json");

        logic.execute("edit-participant E1 P1 /role Host /tag VIP");
        logic.execute("edit 1 n/Updated Person");
        logic.execute("edit-event E1 Updated Workshop");
        logic.execute("archive-event E1");

        assertEquals(1, participationStorage.saveAttempts);
        assertEquals(Set.of(new ParticipationRole("Host")), getOnlyStoredParticipation().getRoles());
        assertEquals(Set.of(new Tag("VIP")), getOnlyStoredParticipation().getTags());
    }

    @Test
    public void execute_editAndUnmarkParticipant_persistsChanges() throws Exception {
        logic.execute("add-participant E1 P1 /role Host /tag VIP");

        logic.execute("edit-participant E1 P1 /role Speaker");
        assertEquals(Set.of(new ParticipationRole("Speaker")), getOnlyStoredParticipation().getRoles());
        logic.execute("edit-participant E1 P1 /tag Guest");
        assertEquals(Set.of(new Tag("Guest")), getOnlyStoredParticipation().getTags());
        logic.execute("mark-participant E1 P1");
        assertTrue(getOnlyStoredParticipation().isPresent());
        logic.execute("unmark-participant E1 P1");
        assertFalse(getOnlyStoredParticipation().isPresent());
        assertEquals(5, participationStorage.saveAttempts);
    }

    @Test
    public void execute_deleteEventAndPerson_persistsCascadingRemovals() throws Exception {
        model.addEvent(new Event(new EventId("E2"), "Concert", 10, "Live music"));
        logic.execute("add-participant E1 P1");
        logic.execute("add-participant E2 P1");

        logic.execute("del-event E1");
        assertEquals("E2", getOnlyStoredParticipation().getEvent().getId());
        logic.execute("delete 1");
        assertTrue(readStoredParticipations().isEmpty());
        assertEquals(4, participationStorage.saveAttempts);
    }

    @Test
    public void execute_participationSaveFailure_retriesUntilSaved() throws Exception {
        logic.execute("add-participant E1 P1");
        IOException saveFailure = new AccessDeniedException("participations.json");
        participationStorage.saveFailure = saveFailure;
        String expectedMessage = String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, saveFailure.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("mark-participant E1 P1"));
        assertFalse(getOnlyStoredParticipation().isPresent());
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("list-event"));
        assertEquals(3, participationStorage.saveAttempts);

        participationStorage.saveFailure = null;
        logic.execute("list-event");
        assertTrue(getOnlyStoredParticipation().isPresent());
        assertEquals(4, participationStorage.saveAttempts);

        participationStorage.saveFailure = saveFailure;
        assertTrue(logic.execute("help").shouldShowHelp());
        assertTrue(logic.execute("exit").shouldExit());
        assertEquals(4, participationStorage.saveAttempts);
    }

    @Test
    public void execute_addParticipant_addsAndPersistsParticipation() throws Exception {
        CommandResult result = logic.execute(
                "add-participant E1 P1 /role Facilitator /role Guest Speaker /tag VIP /tag Alumni");

        assertEquals(String.format(AddParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
        Participation participation = getOnlyStoredParticipation();
        assertEquals(EVENT, participation.getEvent());
        assertEquals(PERSON, participation.getPerson());
        assertFalse(participation.isPresent());
        assertEquals(Set.of(new ParticipationRole("Facilitator"), new ParticipationRole("Guest Speaker")),
                participation.getRoles());
        assertEquals(Set.of(new Tag("VIP"), new Tag("Alumni")), participation.getTags());
    }

    @Test
    public void execute_deleteParticipant_deletesAndPersistsParticipation() throws Exception {
        logic.execute("add-participant E1 P1");

        CommandResult result = logic.execute("del-participant E1 P1");

        assertEquals(String.format(DeleteParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    @Test
    public void execute_markParticipant_marksAndPersistsParticipation() throws Exception {
        logic.execute("add-participant E1 P1");

        CommandResult result = logic.execute("mark-participant E1 P1");

        assertEquals(String.format(MarkParticipantCommand.MESSAGE_SUCCESS, EVENT_ID, PERSON_ID),
                result.getFeedbackToUser());
        assertTrue(getOnlyStoredParticipation().isPresent());
    }

    @Test
    public void execute_addDuplicateParticipant_throwsAndPreservesParticipation() throws Exception {
        logic.execute("add-participant E1 P1");
        String expectedMessage = String.format(
                AddParticipantCommand.MESSAGE_DUPLICATE_PARTICIPANT, EVENT_ID, PERSON_ID);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("add-participant E1 P1"));
        assertFalse(getOnlyStoredParticipation().isPresent());
    }

    @Test
    public void execute_addParticipantWithMissingEvent_throwsAndDoesNotPersist() throws Exception {
        EventId missingEventId = new EventId("E2");
        String expectedMessage = String.format(AddParticipantCommand.MESSAGE_EVENT_NOT_FOUND, missingEventId);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("add-participant E2 P1"));
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    @Test
    public void execute_addParticipantWithMissingPerson_throwsAndDoesNotPersist() throws Exception {
        PersonId missingPersonId = new PersonId("P2");
        String expectedMessage = String.format(AddParticipantCommand.MESSAGE_PERSON_NOT_FOUND, missingPersonId);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("add-participant E1 P2"));
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    @Test
    public void execute_deleteMissingParticipant_throwsAndDoesNotPersist() throws Exception {
        String expectedMessage = String.format(
                DeleteParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND, EVENT_ID, PERSON_ID);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("del-participant E1 P1"));
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    @Test
    public void execute_markMissingParticipant_throwsAndDoesNotPersist() throws Exception {
        String expectedMessage = String.format(
                MarkParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND, EVENT_ID, PERSON_ID);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("mark-participant E1 P1"));
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    @Test
    public void execute_markPresentParticipant_throwsAndPreservesParticipation() throws Exception {
        logic.execute("add-participant E1 P1");
        logic.execute("mark-participant E1 P1");
        String expectedMessage = String.format(
                MarkParticipantCommand.MESSAGE_ALREADY_PRESENT, EVENT_ID, PERSON_ID);

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("mark-participant E1 P1"));
        assertTrue(getOnlyStoredParticipation().isPresent());
    }

    @Test
    public void execute_participantCommandWithInvalidEventId_throwsAndDoesNotPersist() throws Exception {
        assertThrows(ParseException.class, EventId.MESSAGE_CONSTRAINTS, () -> logic.execute("add-participant 1 P1"));
        assertTrue(readStoredParticipations().isEmpty());
        assertTrue(model.getParticipations().getParticipations().isEmpty());
    }

    private Logic createLogic() {
        return new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")), participationStorage));
    }

    /**
     * Writes real participation files, with controllable write failures and a save-attempt count.
     */
    private static class ParticipationStorageStub extends JsonParticipationStorage {
        private int saveAttempts;
        private IOException saveFailure;

        ParticipationStorageStub(Path filePath) {
            super(filePath);
        }

        @Override
        public void saveParticipations(ReadOnlyParticipations participations) throws IOException {
            saveAttempts++;
            if (saveFailure != null) {
                throw saveFailure;
            }
            super.saveParticipations(participations);
        }
    }

    private Participation getOnlyStoredParticipation() throws Exception {
        Set<Participation> participations = readStoredParticipations();
        assertEquals(1, participations.size());
        return participations.iterator().next();
    }

    private Set<Participation> readStoredParticipations() throws Exception {
        return participationStorage.readParticipations(eventList, addressBook)
                .map(ReadOnlyParticipations::getParticipations)
                .orElse(Set.of());
    }
}
