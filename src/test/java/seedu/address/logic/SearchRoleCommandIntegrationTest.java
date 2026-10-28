package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private static final Event ARCHIVED_EVENT = new Event(new EventId("E4"), "Archived", 10, "", true);
    private static final List<Event> EVENTS = List.of(WEDDING, WORKSHOP, EMPTY_EVENT, ARCHIVED_EVENT);
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
            new Participation(WORKSHOP, DAN, true, Set.of(), Set.of(new ParticipationRole("Best Man"))),
            new Participation(ARCHIVED_EVENT, CARA, false, Set.of(), Set.of(new ParticipationRole("Best Man"))));
    private static final String BEST_MAN_RESULT =
            "2 registrations across 1 event with roles matching \"Best Man\".";

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
    public void execute_savedRoles_returnsResultsAndPreservesAllRecords() throws Exception {
        CommandResult result = logic.execute("search-role Best Man /event E1");

        assertEquals(BEST_MAN_RESULT, result.getFeedbackToUser());
        assertEquals(List.of(WEDDING), logic.getFilteredEventList());
        assertEquals(List.of("P1", "P2"), logic.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertTrue(result.isShowEvents());
        assertFalse(result.isShowParticipants());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_partialMixedCaseQueryWithTabs_matchesSavedRoles() throws Exception {
        CommandResult result = logic.execute(" \tsearch-role\t bEsT \t/event\tE1\t ");

        assertEquals("2 registrations across 1 event with roles matching \"bEsT\".",
                result.getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_roleOnlyInOtherEvent_returnsNoMatchesAndPreservesData() throws Exception {
        assertEquals("No registrations with roles matching \"Organizer\" in nonarchived events.",
                logic.execute("search-role Organizer /event E1").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_emptyEvent_returnsNoMatchesAndPreservesData() throws Exception {
        assertEquals("No registrations with roles matching \"Best Man\" in nonarchived events.",
                logic.execute("search-role Best Man /event E3").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_repeatedSearches_usesFullEventRecordsForEachQuery() throws Exception {
        assertEquals(BEST_MAN_RESULT, logic.execute("search-role Best Man /event E1").getFeedbackToUser());
        assertEquals("1 registration across 1 event with roles matching \"Speaker\".",
                logic.execute("search-role Speaker /event E1").getFeedbackToUser());
        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                logic.execute("search-role Best Man /event E2").getFeedbackToUser());
        assertEquals(BEST_MAN_RESULT, logic.execute("search-role Best Man /event E1").getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_activeFilters_replacesEventFilterAndSearchesHiddenEvent() throws Exception {
        model.updateFilteredEventList(event -> event.equals(WORKSHOP));
        model.updateFilteredPersonList(person -> person.equals(CARA));

        assertEquals(BEST_MAN_RESULT, logic.execute("search-role Best Man /event E1").getFeedbackToUser());

        assertEquals(List.of(WEDDING), logic.getFilteredEventList());
        assertEquals(List.of(CARA), logic.getFilteredPersonList());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_invalidInput_throwsParseExceptionAndPreservesFiles() throws Exception {
        logic.execute("search-role Best Man");
        List<Participation> previousResults = List.copyOf(logic.getParticipantSearchResults());
        List<Event> previousEvents = List.copyOf(logic.getFilteredEventList());
        String addressBookJson = Files.readString(storage.getAddressBookFilePath());
        String eventsJson = Files.readString(storage.getEventFilePath());
        String participationsJson = Files.readString(storage.getParticipationFilePath());
        String usageError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE);

        for (String input : List.of("search-role Best Man /event", "search-role /event E1",
                "search-role Best Man /event E1 /event E2", "search-role E1 /role Best Man")) {
            assertThrows(ParseException.class, usageError, () -> logic.execute(input));
        }
        assertThrows(ParseException.class, EventId.MESSAGE_CONSTRAINTS, () ->
                logic.execute("search-role Best Man /event 1"));

        assertEquals(previousResults, logic.getParticipantSearchResults());
        assertEquals(previousEvents, logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertEquals("Best Man", logic.getParticipantSearchKeyword().orElseThrow());
        assertEquals(addressBookJson, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(eventsJson, Files.readString(storage.getEventFilePath()));
        assertEquals(participationsJson, Files.readString(storage.getParticipationFilePath()));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_missingEvent_throwsCommandExceptionAndPreservesData() throws Exception {
        assertThrows(CommandException.class, "Event E99 does not exist.", () ->
                logic.execute("search-role Best Man /event E99"));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_afterReload_returnsSameResultsAndRetainsAllData() throws Exception {
        CommandResult beforeReload = logic.execute("search-role Best Man /event E1");
        model = readStoredModel();
        logic = new LogicManager(model, storage);

        assertTrue(logic.getParticipantSearchResults().isEmpty());
        assertTrue(logic.getParticipantSearchKeyword().isEmpty());
        assertEquals(beforeReload, logic.execute("search-role Best Man /event E1"));
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_globalSearch_exposesOnlyNonarchivedMatchesAcrossEvents() throws Exception {
        CommandResult result = logic.execute("search-role Best Man");
        assertEquals("3 registrations across 2 events with roles matching \"Best Man\".", result.getFeedbackToUser());
        assertEquals(List.of(WEDDING, WORKSHOP), logic.getFilteredEventList());
        assertEquals(List.of("E1/P1", "E1/P2", "E2/P4"), logic.getParticipantSearchResults().stream()
                .map(participation -> participation.getEvent().getId() + "/" + participation.getPerson().getId())
                .toList());
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertEquals("Best Man", logic.getParticipantSearchKeyword().orElseThrow());
        assertTrue(result.isShowEvents());
        assertMemoryAndStorageUnchanged();
    }

    @Test
    public void execute_addEventDuringSearch_preservesFiltersAndRefreshesAfterRegistration() throws Exception {
        for (String search : List.of("search-participant Cara", "search-role Best Man")) {
            for (String scope : List.of("", " /event E4")) {
                for (boolean includeArchived : List.of(false, true)) {
                    logic.execute(search + scope + (includeArchived ? " /include-archive" : ""));
                    List<Event> events = new ArrayList<>(logic.getFilteredEventList());
                    List<Participation> matches = new ArrayList<>(logic.getParticipantSearchResults());
                    String keyword = search.equals("search-participant Cara") ? "Cara" : "Best Man";
                    Optional<Event> scopedEvent = scope.isEmpty() ? Optional.empty() : Optional.of(ARCHIVED_EVENT);

                    CommandResult result = logic.execute("add-event New event /cap 10 /desc No participants");

                    Event added = model.getEvents().getLast();
                    assertEquals("New event", added.getName());
                    assertEquals(10, added.getCapacity());
                    assertEquals("No participants", added.getDescription());
                    assertEquals(new CommandResult("New event added: New event (" + added.getId() + ")",
                            false, false, true), result);
                    assertEquals(events, logic.getFilteredEventList());
                    assertEquals(matches, logic.getParticipantSearchResults());
                    assertSearchContext(keyword, scopedEvent, includeArchived);
                    Model stored = readStoredModel();
                    assertEquals(model.getEvents(), stored.getEvents());
                    assertEquals(PARTICIPATIONS, stored.getParticipations().getParticipations());

                    logic.execute("add-participant " + added.getId() + " P3 /role Best Man");

                    Participation registration = new Participation(added, CARA, false, Set.of(),
                            Set.of(new ParticipationRole("Best Man")));
                    if (scope.isEmpty()) {
                        events.add(added);
                        matches.add(registration);
                    }
                    assertEquals(events, logic.getFilteredEventList());
                    assertEquals(matches, logic.getParticipantSearchResults());
                    assertSearchContext(keyword, scopedEvent, includeArchived);
                    assertEquals(Set.of(registration), logic.getParticipationsForEvent(added));
                    stored = readStoredModel();
                    assertEquals(model.getEvents(), stored.getEvents());
                    assertEquals(model.getParticipations().getParticipations(),
                            stored.getParticipations().getParticipations());

                    logic.execute("del-event " + added.getId());
                    assertMemoryAndStorageUnchanged();
                }
            }
        }
    }

    @Test
    public void execute_addEventDuringEmptySearch_keepsResultsEmptyAndSearchActive() throws Exception {
        for (String search : List.of("search-participant Missing", "search-role Missing")) {
            for (String scope : List.of("", " /event E1")) {
                for (boolean includeArchived : List.of(false, true)) {
                    logic.execute(search + scope + (includeArchived ? " /include-archive" : ""));
                    assertTrue(logic.getFilteredEventList().isEmpty());

                    logic.execute("add-event New event /cap 10 /desc No participants");

                    assertTrue(logic.getFilteredEventList().isEmpty());
                    assertTrue(logic.getParticipantSearchResults().isEmpty());
                    assertSearchContext("Missing", scope.isEmpty() ? Optional.empty() : Optional.of(WEDDING),
                            includeArchived);
                    assertEquals(model.getEvents(), readStoredModel().getEvents());
                }
            }
        }
    }

    @Test
    public void execute_addEventWithoutParticipantSearch_resetsEventFilterAndExcludesArchives() throws Exception {
        for (String command : List.of("list-event", "find-event Workshop", "list-event /include-archive")) {
            logic.execute(command);

            logic.execute("add-event New event /cap 10 /desc No participants");

            assertEquals(model.getEvents().stream().filter(event -> !event.isArchived()).toList(),
                    logic.getFilteredEventList());
            assertTrue(logic.getParticipantSearchResults().isEmpty());
            assertSearchContext(null, Optional.empty(), false);
            assertEquals(model.getEvents(), readStoredModel().getEvents());
        }
    }

    @Test
    public void execute_listAndFindEvents_clearRoleSearchAndPreventLaterReactivation() throws Exception {
        List<String> resetCommands = List.of("list-event", "find-event Workshop", "find-event Missing");
        List<List<Event>> expectedEvents = List.of(List.of(WEDDING, WORKSHOP, EMPTY_EVENT),
                List.of(WORKSHOP), List.of());
        for (String scope : List.of("", " /event E1")) {
            for (int i = 0; i < resetCommands.size(); i++) {
                logic.execute("search-role Best Man" + scope);
                logic.execute(resetCommands.get(i));
                assertEquals(expectedEvents.get(i), logic.getFilteredEventList());
                assertTrue(logic.getParticipantSearchResults().isEmpty());
                assertTrue(logic.getParticipantSearchKeyword().isEmpty());
                assertTrue(logic.getParticipantSearchEvent().isEmpty());

                logic.execute("edit-participant E1 P2 /tag refreshed");
                assertTrue(logic.getParticipantSearchResults().isEmpty());
                assertTrue(logic.getParticipantSearchKeyword().isEmpty());
                assertEquals(expectedEvents.get(i), logic.getFilteredEventList());
            }
        }
    }

    @Test
    public void execute_listAndFindEvents_clearNameSearchAndPreventLaterReactivation() throws Exception {
        for (String scope : List.of("", " /event E1")) {
            for (String command : List.of("list-event", "find-event Workshop")) {
                logic.execute("search-participant Alice" + scope);
                logic.execute(command);
                assertTrue(logic.getParticipantSearchResults().isEmpty());
                assertTrue(logic.getParticipantSearchKeyword().isEmpty());
                assertTrue(logic.getParticipantSearchEvent().isEmpty());
                List<Event> expected = command.equals("list-event")
                        ? List.of(WEDDING, WORKSHOP, EMPTY_EVENT) : List.of(WORKSHOP);
                assertEquals(expected, logic.getFilteredEventList());
                logic.execute("edit-participant E1 P1 /tag refreshed");
                assertTrue(logic.getParticipantSearchResults().isEmpty());
                assertEquals(expected, logic.getFilteredEventList());
            }
        }
    }

    @Test
    public void execute_switchingSearchTypesAndFailedSearches_preserveTheActivePredicate() throws Exception {
        logic.execute("search-role Best Man");
        logic.execute("search-participant Alice /event E1");
        logic.execute("edit-participant E1 P2 /role Best Man");
        assertEquals(List.of("P1"), logic.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals(List.of(WEDDING), logic.getFilteredEventList());

        logic.execute("search-role Speaker");
        assertTrue(logic.getParticipantSearchResults().isEmpty());
        logic.execute("edit-participant E1 P1 /role Speaker");
        assertEquals(List.of("P1"), logic.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());

        logic.execute("search-participant Speaker /event E1");
        logic.execute("edit-participant E1 P2 /role Speaker");
        assertTrue(logic.getParticipantSearchResults().isEmpty());
        assertThrows(CommandException.class, "Event E99 does not exist.", () ->
                logic.execute("search-role Speaker /event E99"));
        assertEquals(WEDDING, logic.getParticipantSearchEvent().orElseThrow());
        assertTrue(logic.getParticipantSearchResults().isEmpty());

        logic.execute("search-role Speaker");
        List<Participation> results = List.copyOf(logic.getParticipantSearchResults());
        assertEquals(2, results.size());
        assertThrows(CommandException.class, "Event E99 does not exist.", () ->
                logic.execute("search-participant Alice /event E99"));
        String usageError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, usageError, () -> logic.execute("search-role /event E1"));
        assertEquals(results, logic.getParticipantSearchResults());
        assertEquals(List.of(WEDDING), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        assertEquals("Speaker", logic.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void execute_archiveAndRestore_keepGlobalAndScopedRoleSearchesActive() throws Exception {
        for (String scope : List.of("", " /event E1")) {
            logic.execute("search-role Best Man" + scope);
            logic.execute("archive-event E1");
            assertEquals(scope.isEmpty() ? List.of(WORKSHOP) : List.of(), logic.getFilteredEventList());
            assertEquals(scope.isEmpty() ? 1 : 0, logic.getParticipantSearchResults().size());
            assertEquals("Best Man", logic.getParticipantSearchKeyword().orElseThrow());

            logic.execute("unarchive-event E1");
            assertEquals(scope.isEmpty() ? List.of(WEDDING, WORKSHOP) : List.of(WEDDING), logic.getFilteredEventList());
            assertEquals(scope.isEmpty() ? 3 : 2, logic.getParticipantSearchResults().size());
        }
    }

    @Test
    public void execute_eventDeletion_refreshesGlobalResultsAndClearsDeletedScope() throws Exception {
        logic.execute("search-role Best Man");
        logic.execute("del-event E1");
        assertEquals(List.of(WORKSHOP), logic.getFilteredEventList());
        assertEquals(List.of("P4"), logic.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals("Best Man", logic.getParticipantSearchKeyword().orElseThrow());

        logic.execute("search-role Best Man /event E2");
        logic.execute("del-event E2");
        assertEquals(List.of(EMPTY_EVENT), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchResults().isEmpty());
        assertTrue(logic.getParticipantSearchKeyword().isEmpty());
        assertTrue(logic.getParticipantSearchEvent().isEmpty());
        logic.execute("add-participant E3 P1 /role Best Man");
        assertEquals(List.of(EMPTY_EVENT), logic.getFilteredEventList());
        assertTrue(logic.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void execute_unchangedParticipations_doesNotAttemptFailingSave() throws Exception {
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
        CommandResult result = logic.execute("search-role Best Man /event E1");
        assertEquals(BEST_MAN_RESULT, result.getFeedbackToUser());
        assertMemoryAndStorageUnchanged();
    }

    /** Verifies the active participant search's keyword, event scope, and archive visibility. */
    private void assertSearchContext(String keyword, Optional<Event> scope, boolean includeArchived) {
        assertEquals(Optional.ofNullable(keyword), logic.getParticipantSearchKeyword());
        assertEquals(scope, logic.getParticipantSearchEvent());
        assertEquals(includeArchived, logic.isParticipantSearchIncludingArchived());
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
