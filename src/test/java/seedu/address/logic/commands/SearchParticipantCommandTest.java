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
        for (String keyword : List.of("", " ", "\t\n", "\u2003")) {
            assertThrows(IllegalArgumentException.class, () -> new SearchParticipantCommand(FIRST_EVENT, keyword));
        }
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchParticipantCommand(FIRST_EVENT, "Michael")
                .execute(null));
    }

    @Test
    public void execute_multipleMatches_returnsCount() throws Exception {
        assertEquals("2 participants matching \"Michael\" found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Michael"));
        assertEquals(List.of("P1", "P2"), model.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals(WEDDING, model.getParticipantSearchEvent().orElseThrow());
        assertEquals("Michael", model.getParticipantSearchKeyword().orElseThrow());
    }

    @Test
    public void execute_oneMatch_returnsSingularMessage() throws Exception {
        assertEquals("1 participant matching \"Michael Tan\" found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Michael Tan"));
    }

    @Test
    public void execute_caseInsensitivePartialMatch_preservesQueryInFeedback() throws Exception {
        assertEquals("2 participants matching \"cHaE\" found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "  cHaE  "));
    }

    @Test
    public void execute_matchOnlyInAnotherEvent_returnsNoMatches() throws Exception {
        assertEquals("No participants matching \"Jordan\" were found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Jordan"));
    }

    @Test
    public void execute_personNotRegisteredForEvent_returnsNoMatches() throws Exception {
        model.removeParticipation(WEDDING, MICHAEL);
        assertEquals("No participants matching \"Michael Tan\" were found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Michael Tan"));
    }

    @Test
    public void execute_emptyEvent_returnsNoMatches() throws Exception {
        model.removeParticipation(WORKSHOP, JORDAN);
        assertEquals("No participants matching \"Jordan\" were found in Workshop.",
                executeSearch(new EventId("E2"), "Jordan"));
    }

    @Test
    public void execute_filteredEvents_searchesByIdEvenWhenHidden() throws Exception {
        model.updateFilteredEventList(event -> event.equals(WORKSHOP));
        assertEquals("2 participants matching \"Michael\" found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Michael"));
        assertEquals(List.of(WORKSHOP), model.getFilteredEventList());
        model.updateFilteredEventList(event -> false);
        assertEquals("1 participant matching \"Jordan\" found in Workshop.",
                executeSearch(new EventId("E2"), "Jordan"));
        assertTrue(model.getFilteredEventList().isEmpty());
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
            public void updateParticipantSearch(EventId eventId, String keyword) {
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
        assertEquals("2 participants matching \"Michael\" found in Jordan's Wedding.",
                executeSearch(FIRST_EVENT, "Michael"));
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_repeatedSearches_preservesDataAndFilters() throws Exception {
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
        assertEquals(List.of(WEDDING), model.getFilteredEventList());
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
        assertEquals("2 participants matching \"Michael\" found in Jordan's Wedding.",
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
                .parseCommand("search-participant E1 /name Michael").execute(model);
        assertEquals("2 participants matching \"Michael\" found in Jordan's Wedding.", result.getFeedbackToUser());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
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

    private String executeSearch(EventId eventId, String keyword) throws CommandException {
        return new SearchParticipantCommand(eventId, keyword).execute(model).getFeedbackToUser();
    }

    private void assertMissingEvent(EventId eventId) {
        CommandException exception = assertThrows(CommandException.class, () ->
                new SearchParticipantCommand(eventId, "Michael").execute(model));
        assertEquals(String.format(SearchParticipantCommand.MESSAGE_EVENT_NOT_FOUND, eventId), exception.getMessage());
    }
}
