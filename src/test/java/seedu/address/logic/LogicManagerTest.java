package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonEventStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        JsonParticipationStorage participationStorage =
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage, participationStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_deleteAndRestart_doesNotReuseEventId() throws Exception {
        JsonAddressBookStorage people = new JsonAddressBookStorage(temporaryFolder.resolve("restart-people.json"));
        JsonEventStorage events = new JsonEventStorage(temporaryFolder.resolve("restart-events.json"));
        StorageManager storage = new StorageManager(people,
                new JsonUserPrefsStorage(temporaryFolder.resolve("restart-prefs.json")), events);
        Logic firstSession = new LogicManager(new ModelManager(), storage);
        firstSession.execute("add-event Reuse A /cap 2 /desc first");
        firstSession.execute("add-event Reuse B /cap 2 /desc second");
        firstSession.execute("del-event E2");
        ModelManager reloaded = new ModelManager(new seedu.address.model.AddressBook(), new UserPrefs(),
                storage.readEvents().orElseThrow());
        new LogicManager(reloaded, storage).execute("add-event Reuse C /cap 2 /desc third");
        assertEquals(List.of("E1", "E3"), reloaded.getEvents().stream().map(Event::getId).toList());
        assertEquals(new BigInteger("4"), storage.readEvents().orElseThrow().getNextId());
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_addAndEditPunctuatedName_preservesSavedPerson() throws Exception {
        logic.execute("add n/Mary-Jane O'Connor p/91234568 e/mary@example.com a/2 Main Street");
        Person addedPerson = model.getFilteredPersonList().get(0);
        assertEquals("Mary-Jane O'Connor", addedPerson.getName().fullName);

        logic.execute("edit 1 n/Anne-Marie O'Neill");
        Person editedPerson = model.getFilteredPersonList().get(0);
        assertEquals("Anne-Marie O'Neill", editedPerson.getName().fullName);
        assertEquals(addedPerson.getId(), editedPerson.getId());

        JsonAddressBookStorage storage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        Model reloadedModel = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(editedPerson), reloadedModel.getFilteredPersonList());
    }

    @Test
    public void execute_addEvent_savesEventsAndPeopleIndependently() throws Exception {
        logic.execute("add-event Party /cap 10 /desc Welcome");
        JsonEventStorage eventStorage = new JsonEventStorage(temporaryFolder.resolve("events.json"));
        assertEquals(model.getEvents(), eventStorage.readEvents().orElseThrow().getEvents());

        model.updateFilteredEventList(unused -> false);
        logic.execute("list");
        assertEquals(model.getEvents(), eventStorage.readEvents().orElseThrow().getEvents());
        JsonAddressBookStorage personStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        assertEquals(model.getAddressBook(), personStorage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_eventSaveFailure_throwsCommandException() {
        JsonAddressBookStorage personStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonEventStorage failingStorage = new JsonEventStorage(temporaryFolder.resolve("events.json")) {
            @Override
            public void saveEvents(List<Event> events, BigInteger nextEventId) throws IOException {
                throw DUMMY_IO_EXCEPTION;
            }
        };
        logic = new LogicManager(model, new StorageManager(personStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")), failingStorage));
        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), () ->
                logic.execute("add-event Party /cap 10 /desc Welcome"));
        // As with person commands, a storage failure is reported after the in-memory change.
        assertEquals(List.of(new Event(new EventId("E1"), "Party", 10, "Welcome")), model.getEvents());
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void execute_findEvent_updatesExistingEventView() throws Exception {
        Event workshop = new Event(new EventId("E1"), "Workshop", 10, "Learn Java");
        Event concert = new Event(new EventId("E2"), "Concert", 20, "Live music");
        model.addEvent(workshop);
        model.addEvent(concert);
        ObservableList<Event> displayedEvents = logic.getFilteredEventList();

        logic.execute("find-event work");
        assertEquals(List.of(workshop), displayedEvents);

        logic.execute("find-event concert");
        assertEquals(List.of(concert), displayedEvents);
        assertThrows(UnsupportedOperationException.class, () -> displayedEvents.add(workshop));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        JsonParticipationStorage participationStorage =
                new JsonParticipationStorage(temporaryFolder.resolve("ExceptionParticipations.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage, participationStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withId("P1").withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
