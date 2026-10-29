package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.EventNotFoundException;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests role search results, optional event scopes, errors, and preservation of stored data.
 */
public class SearchRoleCommandTest {
    private static final EventId EVENT_ID = new EventId("E1");
    private static final EventId OTHER_EVENT_ID = new EventId("E2");
    private static final Event EVENT = new Event(EVENT_ID, "Jordan's Wedding", 20, "Wedding celebration");
    private static final Event OTHER_EVENT = new Event(OTHER_EVENT_ID, "Workshop", 10, "Learn Java");
    private static final Person ALICE = new PersonBuilder().withId("P1").withName("Alice Tan").build();
    private static final Person BOB = new PersonBuilder().withId("P2").withName("Bob Lim").build();

    @Test
    public void constructor_nullArgument_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand(null, "Best Man"));
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand(EVENT_ID, null));
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand((String) null));
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand(null, "Best Man", true));
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand(EVENT_ID, null, true));
        assertThrows(NullPointerException.class, () -> new SearchRoleCommand(null, true));
    }

    @Test
    public void constructor_blankQuery_throwsIllegalArgumentException() {
        for (String keyword : List.of("", " \t\n ", "\u2003", "\u00a0")) {
            assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(EVENT_ID, keyword));
            assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(keyword));
            assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(EVENT_ID, keyword, true));
            assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(keyword, true));
        }
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        SearchRoleCommand command = new SearchRoleCommand(EVENT_ID, "Best Man");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_matchingParticipants_updatesResultsAndReturnsCount() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, BOB, "Best Man"), participation(EVENT, ALICE, "Best Man"));

        CommandResult result = new SearchRoleCommand(EVENT_ID, "Best Man").execute(model);

        assertEquals("2 registrations across 1 event with roles matching \"Best Man\".", result.getFeedbackToUser());
        assertEquals(List.of("P1", "P2"), model.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
        assertEquals(List.of(EVENT), model.getFilteredEventList());
        assertEquals(Optional.of(EVENT), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Best Man"), model.getParticipantSearchKeyword());
        assertFalse(result.shouldShowHelp());
        assertFalse(result.shouldExit());
        assertTrue(result.shouldShowEvents());
        assertFalse(result.shouldShowParticipants());
    }

    @Test
    public void execute_partialMixedCaseQuery_returnsOnlyMatchingParticipants() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, ALICE, "Assistant Best Man"), participation(EVENT, BOB, "Usher"));

        assertEquals("1 registration across 1 event with roles matching \"bEsT\".",
                new SearchRoleCommand(EVENT_ID, "bEsT").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_extraQueryWhitespace_normalizesFeedbackAndMatching() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));

        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand(EVENT_ID, " \tBest  \n Man ").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_multipleMatchingRoles_countsRegistrationOnce() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, ALICE, "Usher", "Best Man", "Assistant Best Man"));

        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
        assertEquals(Set.of(new ParticipationRole("Usher"), new ParticipationRole("Best Man"),
                new ParticipationRole("Assistant Best Man")),
                model.getParticipantSearchResults().getFirst().getRoles());
    }

    @Test
    public void execute_matchingRolesInAnotherEvent_excludesOtherEvent() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Usher"),
                participation(OTHER_EVENT, ALICE, "Best Man"), participation(EVENT, BOB, "Best Man"));

        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_emptyEvent_returnsNoMatchesMessage() throws CommandException {
        assertEquals("No registrations with roles matching \"Best Man\" in nonarchived events.",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(modelWithParticipations()).getFeedbackToUser());
    }

    @Test
    public void execute_noMatchingRoles_returnsNoMatchesMessage() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Usher"),
                participation(EVENT, BOB));

        assertEquals("No registrations with roles matching \"Best Man\" in nonarchived events.",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_missingEvent_throwsCommandException() {
        SearchRoleCommand command = new SearchRoleCommand(new EventId("E99"), "Best Man");

        ModelManager model = modelWithParticipations();
        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals("Event E99 does not exist.", exception.getMessage());
    }

    @Test
    public void execute_mapperCannotResolveEvent_wrapsException() {
        EventNotFoundException cause = new EventNotFoundException();
        ModelManager model = new ModelManager() {
            @Override
            public Set<Participation> getParticipationsForEvent(Event event) {
                throw cause;
            }
        };
        model.addEvent(EVENT);
        SearchRoleCommand command = new SearchRoleCommand(EVENT_ID, "Best Man");

        CommandException exception = assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals("Event E1 does not exist.", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    public void execute_sameNamesAndRoles_preservesDistinctPersonIds() throws CommandException {
        Person firstMichael = new PersonBuilder().withId("P1").withName("Michael").build();
        Person secondMichael = new PersonBuilder(firstMichael).withId("P27").build();
        Set<Participation> participations = Set.of(participation(EVENT, firstMichael, "Best Man"),
                participation(EVENT, secondMichael, "Best Man"));
        // The address book rejects equal names, so supply these records at the model query boundary.
        ModelManager model = new ModelManager() {
            @Override
            public Set<Participation> getParticipationsForEvent(Event event) {
                assertEquals(EVENT, event);
                return participations;
            }
        };
        model.addEvent(EVENT);

        assertEquals("2 registrations across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
        assertEquals(List.of("P1", "P27"), model.getParticipantSearchResults().stream()
                .map(participation -> participation.getPerson().getId().toString()).toList());
    }

    @Test
    public void execute_activeFilters_replacesEventFilterAndPreservesPeopleFilter() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"),
                participation(EVENT, BOB, "Best Man"));
        model.updateFilteredEventList(event -> event.equals(OTHER_EVENT));
        model.updateFilteredPersonList(person -> person.equals(BOB));

        assertEquals("2 registrations across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
        assertEquals(List.of(EVENT), model.getFilteredEventList());
        assertEquals(List.of(BOB), model.getFilteredPersonList());
    }

    @Test
    public void execute_matchingAndNonmatchingQueries_preserveAllStoredData() throws CommandException {
        Participation presentAlice = new Participation(EVENT, ALICE, true, Set.of(new Tag("vip")),
                Set.of(new ParticipationRole("Best Man"), new ParticipationRole("Usher")));
        ModelManager model = modelWithParticipations(presentAlice, participation(EVENT, BOB, "Speaker"),
                participation(OTHER_EVENT, ALICE, "Organizer"));
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        List<Event> originalEvents = List.copyOf(model.getEvents());
        Set<Participation> originalParticipations = model.getParticipations().getParticipations();

        new SearchRoleCommand(EVENT_ID, "Best Man").execute(model);
        new SearchRoleCommand(EVENT_ID, "No Match").execute(model);

        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(originalEvents, model.getEvents());
        assertEquals(originalParticipations, model.getParticipations().getParticipations());
    }

    @Test
    public void equals_sameEventAndNormalizedFeedbackQuery_returnsTrue() {
        SearchRoleCommand command = new SearchRoleCommand(EVENT_ID, "Best Man");
        SearchRoleCommand equivalent = new SearchRoleCommand(new EventId("E1"), "  Best \t Man  ");

        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(equivalent, command);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new SearchRoleCommand(OTHER_EVENT_ID, "Best Man"));
        assertNotEquals(command, new SearchRoleCommand(EVENT_ID, "Usher"));
        assertNotEquals(command, new SearchRoleCommand(EVENT_ID, "best man"));
        assertNotEquals(command, null);
        assertNotEquals(command, "Best Man");
    }

    @Test
    public void execute_globalSearch_countsRegistrationsAcrossEvents() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man", "Assistant Best Man"),
                participation(EVENT, BOB, "Best Man"), participation(OTHER_EVENT, ALICE, "Best Man"));
        model.updateFilteredEventList(event -> false);

        CommandResult result = new SearchRoleCommand("Best Man").execute(model);

        assertEquals("3 registrations across 2 events with roles matching \"Best Man\".", result.getFeedbackToUser());
        assertEquals(List.of(EVENT, OTHER_EVENT), model.getFilteredEventList());
        assertEquals(3, model.getParticipantSearchResults().size());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertTrue(result.shouldShowEvents());
        assertFalse(result.shouldShowParticipants());
    }

    @Test
    public void execute_archivedEventExcludedFromGlobalAndScopedSearches() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"),
                participation(OTHER_EVENT, BOB, "Best Man"));
        model.updateEvent(EVENT.withArchived(true));

        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                new SearchRoleCommand("Best Man").execute(model).getFeedbackToUser());
        assertEquals(List.of(OTHER_EVENT), model.getFilteredEventList());
        assertEquals(OTHER_EVENT, model.getParticipantSearchResults().getFirst().getEvent());
        CommandResult scoped = new SearchRoleCommand(EVENT_ID, "Best Man").execute(model);
        assertEquals("No registrations with roles matching \"Best Man\" in nonarchived events.",
                scoped.getFeedbackToUser());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertTrue(scoped.shouldShowEvents());
    }

    @Test
    public void execute_missingEventAfterGlobalSearch_preservesResultsAndFilters() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));
        new SearchRoleCommand("Best Man").execute(model);
        List<Participation> before = List.copyOf(model.getParticipantSearchResults());

        for (String eventId : List.of("E99", "E001", "E999999999999999")) {
            CommandException exception = assertThrows(CommandException.class, () ->
                    new SearchRoleCommand(new EventId(eventId), "Speaker").execute(model));
            assertEquals("Event " + eventId + " does not exist.", exception.getMessage());
        }
        assertEquals(before, model.getParticipantSearchResults());
        assertEquals(List.of(EVENT), model.getFilteredEventList());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertEquals(Optional.of("Best Man"), model.getParticipantSearchKeyword());
    }

    @Test
    public void execute_emptyResultsAfterSuccessfulSearch_clearsPreviousViewAndSelectsEvents() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));
        new SearchRoleCommand("Best Man").execute(model);
        CommandResult result = new SearchRoleCommand("Missing").execute(model);
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertEquals("No registrations with roles matching \"Missing\" in nonarchived events.",
                result.getFeedbackToUser());
        assertTrue(result.shouldShowEvents());
        assertFalse(result.shouldShowParticipants());
    }

    @Test
    public void equals_globalSearches_distinguishScopeAndNormalizeWhitespace() {
        SearchRoleCommand command = new SearchRoleCommand("Best Man");
        SearchRoleCommand equivalent = new SearchRoleCommand(" \u2003Best\u00a0Man\t ");
        assertEquals(command, equivalent);
        assertEquals(equivalent, command);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new SearchRoleCommand(EVENT_ID, "Best Man"));
        assertNotEquals(command, new SearchRoleCommand("best man"));
        assertNotEquals(command, new SearchRoleCommand("Usher"));
    }

    @Test
    public void parseAndExecute_archiveFlag_countsArchivedMatchesAndResetsOnNextDefaultSearch() throws Exception {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man", "Assistant Best Man"),
                participation(EVENT, BOB, "Best Man"), participation(OTHER_EVENT, ALICE, "Best Man"));
        model.updateEvent(EVENT.withArchived(true));
        AddressBookParser parser = new AddressBookParser();

        CommandResult result = parser.parseCommand("search-role Best Man /include-archive").execute(model);

        assertEquals("3 registrations across 2 events with roles matching \"Best Man\".", result.getFeedbackToUser());
        assertEquals(List.of(EVENT.withArchived(true), OTHER_EVENT), model.getFilteredEventList());
        assertEquals(3, model.getParticipantSearchResults().size());
        assertTrue(result.shouldShowEvents());
        assertFalse(result.shouldShowHelp());
        assertFalse(result.shouldExit());

        CommandResult defaultResult = parser.parseCommand("search-role Best Man").execute(model);
        assertEquals("1 registration across 1 event with roles matching \"Best Man\".",
                defaultResult.getFeedbackToUser());
        assertEquals(List.of(OTHER_EVENT), model.getFilteredEventList());
    }

    @Test
    public void parseAndExecute_archivedScope_searchesOnlyRequestedEventAndResetsOnDefaultSearch() throws Exception {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"),
                participation(OTHER_EVENT, BOB, "Best Man"));
        model.updateEvent(EVENT.withArchived(true));
        AddressBookParser parser = new AddressBookParser();

        CommandResult result = parser.parseCommand("search-role Best Man /event E1 /include-archive").execute(model);

        assertEquals("1 registration across 1 event with roles matching \"Best Man\".", result.getFeedbackToUser());
        assertEquals(List.of(EVENT.withArchived(true)), model.getFilteredEventList());
        assertEquals(Optional.of(EVENT.withArchived(true)), model.getParticipantSearchEvent());

        CommandResult defaultResult = parser.parseCommand("search-role Best Man /event E1").execute(model);
        assertEquals("No registrations with roles matching \"Best Man\" in nonarchived events.",
                defaultResult.getFeedbackToUser());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertTrue(model.getFilteredEventList().isEmpty());
    }

    @Test
    public void execute_archiveFlagWithoutMatches_reportsArchiveInclusion() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));
        model.updateEvent(EVENT.withArchived(true));
        for (SearchRoleCommand command : List.of(new SearchRoleCommand("Missing", true),
                new SearchRoleCommand(EVENT_ID, "Missing", true))) {
            CommandResult result = command.execute(model);
            assertEquals("No registrations with roles matching \"Missing\" (including archived events).",
                    result.getFeedbackToUser());
            assertTrue(model.getParticipantSearchResults().isEmpty());
            assertTrue(model.getFilteredEventList().isEmpty());
            assertTrue(result.shouldShowEvents());
        }
    }

    @Test
    public void parseAndExecute_invalidSearch_preservesPreviousArchiveResultsAndContext() throws Exception {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));
        model.updateEvent(EVENT.withArchived(true));
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("search-role Best Man /event E1 /include-archive").execute(model);
        List<Participation> results = List.copyOf(model.getParticipantSearchResults());

        assertThrows(ParseException.class, () ->
                parser.parseCommand("search-role Best Man /include-archive /event E1").execute(model));
        CommandException exception = assertThrows(CommandException.class, () ->
                parser.parseCommand("search-role Best Man /event E99").execute(model));
        assertEquals("Event E99 does not exist.", exception.getMessage());
        assertTrue(exception.getCause() instanceof EventNotFoundException);
        assertThrows(CommandException.class, () ->
                parser.parseCommand("search-role Best Man /event E99 /include-archive").execute(model));

        assertEquals(results, model.getParticipantSearchResults());
        assertEquals(List.of(EVENT.withArchived(true)), model.getFilteredEventList());
        assertEquals(Optional.of(EVENT.withArchived(true)), model.getParticipantSearchEvent());
        assertEquals(Optional.of("Best Man"), model.getParticipantSearchKeyword());
        model.setPresent(EVENT, ALICE, true);
        assertEquals(1, model.getParticipantSearchResults().size());
        assertEquals(List.of(EVENT.withArchived(true)), model.getFilteredEventList());
    }

    @Test
    public void equalsAndHashCode_archiveVisibility_distinguishesCommandsInBothScopes() {
        SearchRoleCommand global = new SearchRoleCommand("Best Man", true);
        SearchRoleCommand globalCopy = new SearchRoleCommand(" Best\tMan ", true);
        assertEquals(global, globalCopy);
        assertEquals(global.hashCode(), globalCopy.hashCode());
        assertNotEquals(global, new SearchRoleCommand("Best Man"));
        assertEquals(new SearchRoleCommand("Best Man"), new SearchRoleCommand("Best Man", false));

        SearchRoleCommand scoped = new SearchRoleCommand(EVENT_ID, "Best Man", true);
        SearchRoleCommand scopedCopy = new SearchRoleCommand(EVENT_ID, " Best\tMan ", true);
        assertEquals(scoped, scopedCopy);
        assertEquals(scoped.hashCode(), scopedCopy.hashCode());
        assertNotEquals(scoped, global);
        assertNotEquals(scoped, new SearchRoleCommand(EVENT_ID, "Best Man"));
        assertEquals(new SearchRoleCommand(EVENT_ID, "Best Man"), new SearchRoleCommand(EVENT_ID, "Best Man", false));
    }

    /**
     * Creates a real model initialized with role-bearing records and both fixture events and people.
     */
    private static ModelManager modelWithParticipations(Participation... participations) {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BOB);
        EventList eventList = new EventList();
        eventList.addEvent(EVENT);
        eventList.addEvent(OTHER_EVENT);
        return new ModelManager(addressBook, new UserPrefs(), eventList, () -> Set.of(participations));
    }

    /**
     * Creates a participation with the supplied roles, initially absent and without tags.
     */
    private static Participation participation(Event event, Person person, String... roles) {
        Set<ParticipationRole> participationRoles = Arrays.stream(roles)
                .map(ParticipationRole::new)
                .collect(Collectors.toSet());
        return new Participation(event, person, false, Set.of(), participationRoles);
    }
}
