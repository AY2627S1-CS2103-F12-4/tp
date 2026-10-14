package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.SearchRoleCommand;
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
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonEventStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests role search through the real parser, command, model, mapper, and JSON storage.
 * Fixtures are loaded from disk, and state checks compare both memory and freshly reloaded data.
 */
public class SearchRoleCommandIntegrationTest {
    private static final Event WEDDING = new Event(new EventId("E1"), "Jordan's Wedding", 20, "Celebration");
    private static final Event WORKSHOP = new Event(new EventId("E2"), "Workshop", 10, "Learn Java");
    private static final Event EMPTY_EVENT = new Event(new EventId("E3"), "Empty Event", 5, "No participants");
    private static final List<Event> EVENTS = List.of(WEDDING, WORKSHOP, EMPTY_EVENT);
    private static final Person ALICE = new PersonBuilder().withId("P1").withName("Alice Tan").build();
    private static final Person BOB = new PersonBuilder().withId("P2").withName("Bob Lim").build();
    private static final Person CARA = new PersonBuilder().withId("P3").withName("Cara Lee").build();
    private static final Person DAN = new PersonBuilder().withId("P4").withName("Dan Ng").build();
    private static final Set<Participation> PARTICIPATIONS = Set.of(
            new Participation(WEDDING, ALICE, true, Set.of(new Tag("vip")),
                    Set.of(new ParticipationRole("Best Man"), new ParticipationRole("Usher"))),
            new Participation(WEDDING, BOB, false, Set.of(new Tag("helper")),
                    Set.of(new ParticipationRole("Assistant Best Man"), new ParticipationRole("Speaker"))),
            new Participation(WEDDING, CARA),
            new Participation(WORKSHOP, ALICE, false, Set.of(), Set.of(new ParticipationRole("Organizer"))),
            new Participation(WORKSHOP, DAN, true, Set.of(), Set.of(new ParticipationRole("Best Man"))));
    private static final String BEST_MAN_RESULT =
            "2 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                    + "P1 — Alice Tan — Best Man, Usher\nP2 — Bob Lim — Assistant Best Man, Speaker";

    @TempDir
    public Path temporaryFolder;

    private AddressBook originalAddressBook;
    private StorageManager storage;
    private Model model;
    private Logic logic;

    /**
     * Saves role-bearing fixtures and loads a fresh model before each test.
     *
     * @throws IOException if fixture data cannot be saved.
     * @throws DataLoadingException if fixture data cannot be read.
     */
    @BeforeEach
    public void setUp() throws IOException, DataLoadingException {
        originalAddressBook = new AddressBook();
        for (Person person : List.of(ALICE, BOB, CARA, DAN)) {
            originalAddressBook.addPerson(person);
        }
        storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")),
                new JsonEventStorage(temporaryFolder.resolve("events.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json")));
        storage.saveAddressBook(originalAddressBook);
        storage.saveEvents(EVENTS);
        storage.saveParticipations(() -> PARTICIPATIONS);
        model = readStoredModel();
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_savedRoles_returnsDetailsAndPreservesAllRecords() throws Exception {
        CommandResult result = logic.execute("search-role E1 /role Best Man");

        assertEquals(BEST_MAN_RESULT, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.isShowEvents());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_partialMixedCaseQueryWithTabs_matchesSavedRoles() throws Exception {
        CommandResult result = logic.execute(" \tsearch-role\tE1\t/role \t bEsT \t ");

        assertEquals("2 participants with roles matching \"bEsT\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Best Man, Usher\nP2 — Bob Lim — Assistant Best Man, Speaker",
                result.getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_roleOnlyInOtherEvent_returnsNoMatchesAndPreservesData() throws Exception {
        assertEquals("No participants with roles matching \"Organizer\" were found in Jordan's Wedding.",
                logic.execute("search-role E1 /role Organizer").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_emptyEvent_returnsNoMatchesAndPreservesData() throws Exception {
        assertEquals("No participants with roles matching \"Best Man\" were found in Empty Event.",
                logic.execute("search-role E3 /role Best Man").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_repeatedSearches_usesFullEventRecordsForEachQuery() throws Exception {
        assertEquals(BEST_MAN_RESULT, logic.execute("search-role E1 /role Best Man").getFeedbackToUser());
        assertEquals("1 participants with roles matching \"Speaker\" found in Jordan's Wedding.\n"
                + "P2 — Bob Lim — Assistant Best Man, Speaker",
                logic.execute("search-role E1 /role Speaker").getFeedbackToUser());
        assertEquals("1 participants with roles matching \"Best Man\" found in Workshop.\n"
                + "P4 — Dan Ng — Best Man",
                logic.execute("search-role E2 /role Best Man").getFeedbackToUser());
        assertEquals(BEST_MAN_RESULT, logic.execute("search-role E1 /role Best Man").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_activeFilters_preservesFiltersAndSearchesHiddenEvent() throws Exception {
        model.updateFilteredEventList(event -> event.equals(WORKSHOP));
        model.updateFilteredPersonList(person -> person.equals(CARA));

        assertEquals(BEST_MAN_RESULT, logic.execute("search-role E1 /role Best Man").getFeedbackToUser());

        assertEquals(List.of(WORKSHOP), logic.getFilteredEventList());
        assertEquals(List.of(CARA), logic.getFilteredPersonList());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_invalidInput_throwsParseExceptionAndPreservesFiles() throws Exception {
        String addressBookJson = Files.readString(storage.getAddressBookFilePath());
        String eventsJson = Files.readString(storage.getEventFilePath());
        String participationsJson = Files.readString(storage.getParticipationFilePath());
        String usageError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE);

        assertThrows(ParseException.class, usageError, () -> logic.execute("search-role E1 /role"));
        assertThrows(ParseException.class, usageError, () -> logic.execute("search-role /role Best Man"));
        assertThrows(ParseException.class, EventId.MESSAGE_CONSTRAINTS, () ->
                logic.execute("search-role 1 /role Best Man"));
        assertThrows(ParseException.class, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE), () ->
                logic.execute("search-role E1 /role Best Man /role Usher"));

        assertEquals(addressBookJson, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(eventsJson, Files.readString(storage.getEventFilePath()));
        assertEquals(participationsJson, Files.readString(storage.getParticipationFilePath()));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_missingEvent_throwsCommandExceptionAndPreservesData() throws Exception {
        assertThrows(CommandException.class, "Event E99 does not exist.", () ->
                logic.execute("search-role E99 /role Best Man"));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_afterReload_returnsSameResultsAndRetainsAllData() throws Exception {
        CommandResult beforeReload = logic.execute("search-role E1 /role Best Man");
        model = readStoredModel();
        logic = new LogicManager(model, storage);

        assertEquals(beforeReload, logic.execute("search-role E1 /role Best Man"));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_participationSaveFailure_reportsErrorWithoutChangingData() throws Exception {
        IOException saveFailure = new IOException("Cannot save participations");
        JsonParticipationStorage failingStorage = new JsonParticipationStorage(storage.getParticipationFilePath()) {
            @Override
            public void saveParticipations(ReadOnlyParticipations participations) throws IOException {
                throw saveFailure;
            }
        };
        StorageManager storageWithSaveFailure = new StorageManager(
                new JsonAddressBookStorage(storage.getAddressBookFilePath()),
                new JsonUserPrefsStorage(storage.getUserPrefsFilePath()),
                new JsonEventStorage(storage.getEventFilePath()), failingStorage);
        logic = new LogicManager(model, storageWithSaveFailure);
        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, saveFailure.getMessage());

        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("search-role E1 /role Best Man"));
        assertMemoryAndStorageUnchanged();
    }

    /**
     * Reconstructs a model from all three data files, resolving participations against reloaded people and events.
     *
     * @throws DataLoadingException if any data file cannot be read.
     */
    private Model readStoredModel() throws DataLoadingException {
        AddressBook storedAddressBook = new AddressBook(storage.readAddressBook().orElseThrow());
        EventList storedEvents = storage.readEvents().orElseThrow();
        ReadOnlyParticipations storedParticipations = storage.readParticipations(storedEvents, storedAddressBook)
                .orElseThrow();
        return new ModelManager(storedAddressBook, new UserPrefs(), storedEvents, storedParticipations);
    }

    /**
     * Verifies all people, IDs, event details, attendance, tags, and roles remain intact in memory and storage.
     *
     * @throws DataLoadingException if saved data cannot be reloaded.
     */
    private void assertMemoryAndStorageUnchanged() throws DataLoadingException {
        for (Model candidate : List.of(model, readStoredModel())) {
            assertEquals(originalAddressBook, candidate.getAddressBook());
            // Person equality omits IDs, so verify them separately.
            assertEquals(List.of("P1", "P2", "P3", "P4"), candidate.getAddressBook().getPersonList().stream()
                    .map(person -> person.getId().toString()).toList());
            assertEquals(EVENTS, candidate.getEvents());
            assertEquals(PARTICIPATIONS, candidate.getParticipations().getParticipations());
        }
    }
}
