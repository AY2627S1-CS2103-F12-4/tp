package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class SearchParticipantCommandTest {
    private static final EventId FIRST_EVENT = new EventId("E1");
    private static final Event WEDDING = new Event(new EventId("E1"), "Jordan's Wedding", 30, "Reception");
    private static final Event WORKSHOP = new Event(new EventId("E2"), "Workshop", 20, "Java");
    private static final Person MICHAEL = new PersonBuilder().withId("P1").withName("Michael Tan").build();
    private static final Person MICHAELA = new PersonBuilder().withId("P2").withName("Michaela Lee").build();
    private static final Person JORDAN = new PersonBuilder().withId("P3").withName("Jordan Lee").build();

    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addEvent(WEDDING);
        model.addEvent(WORKSHOP);
        model.addPerson(MICHAEL);
        model.addPerson(MICHAELA);
        model.addPerson(JORDAN);
        model.addParticipation(WEDDING, MICHAEL);
        model.addParticipation(WEDDING, MICHAELA);
        model.addParticipation(WORKSHOP, JORDAN);
    }

    @Test
    public void constructor_invalidArguments_throwsException() {
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(null, "Michael"));
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(FIRST_EVENT, null));
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand((String) null));
        for (String keyword : List.of("", " ", "\t\n", "\u2003")) {
            assertThrows(IllegalArgumentException.class, () -> new SearchParticipantCommand(FIRST_EVENT, keyword));
            assertThrows(IllegalArgumentException.class, () -> new SearchParticipantCommand(keyword));
            assertThrows(IllegalArgumentException.class, () ->
                    new SearchParticipantCommand(FIRST_EVENT, keyword, true));
            assertThrows(IllegalArgumentException.class, () -> new SearchParticipantCommand(keyword, true));
        }
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(null, "Michael", true));
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(FIRST_EVENT, null, true));
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(null, true));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(FIRST_EVENT, "Michael")
                .execute(null));
    }

    @Test
    public void execute_multipleMatches_returnsCount() throws Exception {
        assertEquals("2 matching registrations across 1 event for \"Michael\".",
                executeSearch(FIRST_EVENT, "Michael"));
        assertEquals(List.of("P1", "P2"), model.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals(WEDDING, model.getParticipantSearchEvent().orElseThrow());
        assertEquals("Michael", model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void execute_oneMatch_returnsSingularMessage() throws Exception {
        assertEquals("1 matching registration across 1 event for \"Michael Tan\".",
                executeSearch(FIRST_EVENT, "Michael Tan"));
    }

    @Test
    public void execute_caseInsensitivePartialMatch_preservesQueryInFeedback() throws Exception {
        assertEquals("2 matching registrations across 1 event for \"cHaE\".",
                executeSearch(FIRST_EVENT, "  cHaE  "));
    }

    @Test
    public void execute_matchOnlyInAnotherEvent_returnsNoMatches() throws Exception {
        assertEquals("No matching registrations for \"Jordan\" in nonarchived events.",
                executeSearch(FIRST_EVENT, "Jordan"));
    }

    @Test
    public void execute_personNotRegisteredForEvent_returnsNoMatches() throws Exception {
        model.removeParticipation(WEDDING, MICHAEL);
        assertEquals("No matching registrations for \"Michael Tan\" in nonarchived events.",
                executeSearch(FIRST_EVENT, "Michael Tan"));
    }

    @Test
    public void execute_emptyEvent_returnsNoMatches() throws Exception {
        model.removeParticipation(WORKSHOP, JORDAN);
        assertEquals("No matching registrations for \"Jordan\" in nonarchived events.",
                executeSearch(new EventId("E2"), "Jordan"));
    }

    @Test
    public void execute_filteredEvents_searchesByIdEvenWhenHidden() throws Exception {
        model.updateFilteredEventList(event -> event.equals(WORKSHOP));
        assertEquals("2 matching registrations across 1 event for \"Michael\".",
                executeSearch(FIRST_EVENT, "Michael"));
        assertEquals(List.of(WEDDING), model.getFilteredEventList());
        model.updateFilteredEventList(event -> false);
        assertEquals("1 matching registration across 1 event for \"Jordan\".",
                executeSearch(new EventId("E2"), "Jordan"));
        assertEquals(List.of(WORKSHOP), model.getFilteredEventList());
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        assertMissingEvent(new EventId("E99"));
        assertMissingEvent(new EventId("E999999999999999"));
    }

    @Test
    public void execute_eventDisappearsDuringSearch_throwsCommandException() {
        ModelManager unavailableEventModel = new ModelManager() {
            @Override
            public void updateParticipantSearch(EventId eventId, String keyword, boolean includeArchived) {
                throw new EventNotFoundException();
            }
        };
        unavailableEventModel.addEvent(WEDDING);
        CommandException exception = assertThrows(CommandException.class, () ->
                new SearchParticipantCommand(FIRST_EVENT, "Michael").execute(unavailableEventModel));
        assertEquals("Event E1 does not exist.", exception.getMessage());
        assertTrue(exception.getCause() instanceof EventNotFoundException);
    }

    @Test
    public void execute_paddedEventId_doesNotAliasUnpaddedId() {
        assertMissingEvent(new EventId("E001"));
    }

    @Test
    public void execute_personFilter_doesNotHideMatchingParticipants() throws Exception {
        model.updateFilteredPersonList(person -> false);
        assertEquals("2 matching registrations across 1 event for \"Michael\".",
                executeSearch(FIRST_EVENT, "Michael"));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_repeatedSearches_preservesDataAndPeopleFilter() throws Exception {
        model.setPresent(WEDDING, MICHAEL, true);
        model.setTags(WEDDING, MICHAEL, Set.of(new Tag("vip")));
        model.updateFilteredPersonList(person -> person.getId().equals(JORDAN.getId()));
        model.updateFilteredEventList(event -> event.equals(WEDDING));
        AddressBook originalPeople = new AddressBook(model.getAddressBook());
        List<Event> originalEvents = model.getEvents();
        Set<Participation> originalParticipations = model.getParticipations().getParticipations();

        executeSearch(FIRST_EVENT, "Michael");
        executeSearch(FIRST_EVENT, "Missing");
        assertMissingEvent(new EventId("E99"));

        assertEquals(originalPeople, model.getAddressBook());
        assertEquals(originalEvents, model.getEvents());
        assertEquals(originalParticipations, model.getParticipations().getParticipations());
        assertEquals(List.of(JORDAN), model.getFilteredPersonList());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_distinctIdsWithIdenticalNamesAndRoles_countsBoth() throws Exception {
        Person anotherMichael = new PersonBuilder(MICHAEL).withId("P27").build();
        Set<ParticipationRole> roles = Set.of(new ParticipationRole("Best Man"));
        Set<Participation> records = Set.of(
                new Participation(WEDDING, MICHAEL, false, Set.of(), roles),
                new Participation(WEDDING, anotherMichael, false, Set.of(), roles));
        // Supply records directly: legacy AddressBook still rejects identical names.
        ModelManager modelWithSameNames = new ModelManager() {
            @Override
            public Set<Participation> getParticipationsForEvent(Event event) {
                assertEquals(WEDDING, event);
                return records;
            }
        };
        modelWithSameNames.addEvent(WEDDING);
        assertEquals("2 matching registrations across 1 event for \"Michael\".",
                new SearchParticipantCommand(FIRST_EVENT, "Michael").execute(modelWithSameNames).getFeedbackToUser());
        assertEquals(List.of("P1", "P27"), modelWithSameNames.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertTrue(modelWithSameNames.getParticipantSearchResults().stream()
                .allMatch(participation -> participation.getRoles().equals(roles)));
    }

    @Test
    public void execute_noMatchesAfterSuccessfulSearch_clearsPreviousResults() throws Exception {
        executeSearch(FIRST_EVENT, "Michael");
        executeSearch(new EventId("E2"), "Missing");
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertEquals(WORKSHOP, model.getParticipantSearchEvent().orElseThrow());
        assertEquals("Missing", model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void execute_missingEventAfterSuccessfulSearch_preservesResults() throws Exception {
        executeSearch(FIRST_EVENT, "Michael");
        List<Participation> results = List.copyOf(model.getParticipantSearchResults());
        assertMissingEvent(new EventId("E99"));
        assertEquals(results, model.getParticipantSearchResults());
        assertEquals(WEDDING, model.getParticipantSearchEvent().orElseThrow());
        assertEquals("Michael", model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void parseAndExecute_validCommand_returnsFeedback() throws Exception {
        CommandResult result = new AddressBookParser()
                .parseCommand("search-participant Michael /event E1").execute(model);
        assertEquals("2 matching registrations across 1 event for \"Michael\".", result.getFeedbackToUser());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
        assertFalse(result.isShowParticipants());
    }

    @Test
    public void execute_zeroOneAndMultipleMatches_alwaysRequestsEventView() throws Exception {
        for (String keyword : List.of("Missing", "Michael Tan", "Michael")) {
            CommandResult result = new SearchParticipantCommand(FIRST_EVENT, keyword).execute(model);
            assertFalse(result.isShowParticipants());
            assertTrue(result.isShowEvents());
        }
    }

    @Test
    public void equalsAndHashCode() {
        SearchParticipantCommand command = new SearchParticipantCommand(FIRST_EVENT, "Michael");
        SearchParticipantCommand copy = new SearchParticipantCommand(FIRST_EVENT, " Michael ");
        assertEquals(command, command);
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertNotEquals(command, null);
        assertNotEquals(command, "Michael");
        assertNotEquals(command, new SearchParticipantCommand(new EventId("E2"), "Michael"));
        assertNotEquals(command, new SearchParticipantCommand(FIRST_EVENT, "Jordan"));
        assertNotEquals(command, new SearchParticipantCommand(FIRST_EVENT, "MICHAEL"));
    }

    @Test
    public void execute_globalSearch_countsRegistrationsSeparatelyAcrossEvents() throws Exception {
        model.addParticipation(WORKSHOP, MICHAEL);
        CommandResult result = new SearchParticipantCommand("Michael").execute(model);
        assertEquals("3 matching registrations across 2 events for \"Michael\".", result.getFeedbackToUser());
        assertEquals(List.of(WEDDING, WORKSHOP), model.getFilteredEventList());
        assertEquals(3, model.getParticipantSearchResults().size());
        assertTrue(result.isShowEvents());
        assertFalse(result.isShowParticipants());
    }

    @Test
    public void execute_archivedScopeAndEmptyGlobalSearch_showEmptyEventResults() throws Exception {
        model.updateEvent(WEDDING.withArchived(true));
        CommandResult scoped = new SearchParticipantCommand(FIRST_EVENT, "Michael").execute(model);
        assertEquals("No matching registrations for \"Michael\" in nonarchived events.", scoped.getFeedbackToUser());
        assertTrue(model.getFilteredEventList().isEmpty());
        CommandResult global = new SearchParticipantCommand("Michael").execute(model);
        assertEquals(scoped, global);
        assertTrue(model.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void equals_globalAndScopedSearches_distinguishScopeAndNormalizeWhitespace() {
        SearchParticipantCommand global = new SearchParticipantCommand("Michael Tan");
        SearchParticipantCommand equivalent = new SearchParticipantCommand(" Michael\t Tan ");
        assertEquals(global, equivalent);
        assertEquals(global.hashCode(), equivalent.hashCode());
        assertNotEquals(global, new SearchParticipantCommand(FIRST_EVENT, "Michael Tan"));
        assertNotEquals(global, new SearchParticipantCommand("MICHAEL TAN"));
    }

    @Test
    public void parseAndExecute_archiveFlag_countsArchivedMatchesAndResetsOnNextDefaultSearch() throws Exception {
        model.updateEvent(WEDDING.withArchived(true));
        model.addParticipation(WORKSHOP, MICHAEL);
        AddressBookParser parser = new AddressBookParser();

        CommandResult result = parser.parseCommand("search-participant Michael /include-archive").execute(model);

        assertEquals("3 matching registrations across 2 events for \"Michael\".", result.getFeedbackToUser());
        assertEquals(List.of(WEDDING.withArchived(true), WORKSHOP), model.getFilteredEventList());
        assertEquals(3, model.getParticipantSearchResults().size());
        assertTrue(result.isShowEvents());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());

        CommandResult defaultResult = parser.parseCommand("search-participant Michael").execute(model);
        assertEquals("1 matching registration across 1 event for \"Michael\".", defaultResult.getFeedbackToUser());
        assertEquals(List.of(WORKSHOP), model.getFilteredEventList());
    }

    @Test
    public void parseAndExecute_archivedScope_searchesOnlyRequestedEventAndResetsOnDefaultSearch() throws Exception {
        model.updateEvent(WEDDING.withArchived(true));
        model.addParticipation(WORKSHOP, MICHAEL);
        AddressBookParser parser = new AddressBookParser();

        CommandResult result = parser.parseCommand("search-participant Michael /event E1 /include-archive")
                .execute(model);

        assertEquals("2 matching registrations across 1 event for \"Michael\".", result.getFeedbackToUser());
        assertEquals(List.of(WEDDING.withArchived(true)), model.getFilteredEventList());
        assertEquals(WEDDING.withArchived(true), model.getParticipantSearchEvent().orElseThrow());

        CommandResult defaultResult = parser.parseCommand("search-participant Michael /event E1").execute(model);
        assertEquals("No matching registrations for \"Michael\" in nonarchived events.",
                defaultResult.getFeedbackToUser());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_archiveFlagWithoutMatches_reportsArchiveInclusion() throws Exception {
        model.updateEvent(WEDDING.withArchived(true));
        for (SearchParticipantCommand command : List.of(new SearchParticipantCommand("Missing", true),
                new SearchParticipantCommand(FIRST_EVENT, "Missing", true))) {
            CommandResult result = command.execute(model);
            assertEquals("No matching registrations for \"Missing\" (including archived events).",
                    result.getFeedbackToUser());
            assertTrue(model.getParticipantSearchResults().isEmpty());
            assertTrue(model.getFilteredEventList().isEmpty());
            assertTrue(result.isShowEvents());
        }
    }

    @Test
    public void parseAndExecute_invalidSearch_preservesPreviousArchiveResultsAndContext() throws Exception {
        model.updateEvent(WEDDING.withArchived(true));
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("search-participant Michael /event E1 /include-archive").execute(model);
        List<Participation> results = List.copyOf(model.getParticipantSearchResults());

        assertThrows(ParseException.class, () ->
                parser.parseCommand("search-participant Michael /include-archive /event E1").execute(model));
        CommandException exception = assertThrows(CommandException.class, () ->
                parser.parseCommand("search-participant Michael /event E99").execute(model));
        assertEquals("Event E99 does not exist.", exception.getMessage());
        assertTrue(exception.getCause() instanceof EventNotFoundException);
        assertThrows(CommandException.class, () ->
                parser.parseCommand("search-participant Michael /event E99 /include-archive").execute(model));

        assertEquals(results, model.getParticipantSearchResults());
        assertEquals(List.of(WEDDING.withArchived(true)), model.getFilteredEventList());
        assertEquals(WEDDING.withArchived(true), model.getParticipantSearchEvent().orElseThrow());
        assertEquals("Michael", model.getParticipantSearchKeyword().orElseThrow());
        model.removeParticipation(WEDDING, MICHAELA);
        assertEquals(1, model.getParticipantSearchResults().size());
        assertEquals(List.of(WEDDING.withArchived(true)), model.getFilteredEventList());
    }

    @Test
    public void equalsAndHashCode_archiveVisibility_distinguishesCommandsInBothScopes() {
        SearchParticipantCommand global = new SearchParticipantCommand("Michael Tan", true);
        SearchParticipantCommand globalCopy = new SearchParticipantCommand(" Michael\tTan ", true);
        assertEquals(global, globalCopy);
        assertEquals(global.hashCode(), globalCopy.hashCode());
        assertNotEquals(global, new SearchParticipantCommand("Michael Tan"));
        assertEquals(new SearchParticipantCommand("Michael Tan"), new SearchParticipantCommand("Michael Tan", false));

        SearchParticipantCommand scoped = new SearchParticipantCommand(FIRST_EVENT, "Michael Tan", true);
        SearchParticipantCommand scopedCopy = new SearchParticipantCommand(FIRST_EVENT, " Michael\tTan ", true);
        assertEquals(scoped, scopedCopy);
        assertEquals(scoped.hashCode(), scopedCopy.hashCode());
        assertNotEquals(scoped, global);
        assertNotEquals(scoped, new SearchParticipantCommand(FIRST_EVENT, "Michael Tan"));
        assertEquals(new SearchParticipantCommand(FIRST_EVENT, "Michael Tan"),
                new SearchParticipantCommand(FIRST_EVENT, "Michael Tan", false));
    }

    private String executeSearch(EventId eventId, String keyword) throws CommandException {
        return new SearchParticipantCommand(eventId, keyword).execute(model).getFeedbackToUser();
    }

    private void assertMissingEvent(EventId eventId) {
        CommandException exception = assertThrows(CommandException.class, () ->
                new SearchParticipantCommand(eventId, "Michael").execute(model));
        assertEquals(String.format(SearchParticipantCommand.MESSAGE_EVENT_NOT_FOUND, eventId), exception.getMessage());
    }
}
