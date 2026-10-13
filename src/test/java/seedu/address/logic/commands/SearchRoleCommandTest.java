package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
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
 * Tests event-scoped role search output, errors, command identity, and preservation of model state.
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
    }

    @Test
    public void constructor_blankQuery_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(EVENT_ID, ""));
        assertThrows(IllegalArgumentException.class, () -> new SearchRoleCommand(EVENT_ID, " \t\n "));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        SearchRoleCommand command = new SearchRoleCommand(EVENT_ID, "Best Man");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_matchingParticipants_returnsSortedDetailsAndCount() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, BOB, "Best Man"), participation(EVENT, ALICE, "Best Man"));

        CommandResult result = new SearchRoleCommand(EVENT_ID, "Best Man").execute(model);

        assertEquals("2 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Best Man\nP2 — Bob Lim — Best Man", result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.isShowEvents());
    }

    @Test
    public void execute_partialMixedCaseQuery_returnsOnlyMatchingParticipants() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, ALICE, "Assistant Best Man"), participation(EVENT, BOB, "Usher"));

        assertEquals("1 participants with roles matching \"bEsT\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Assistant Best Man",
                new SearchRoleCommand(EVENT_ID, "bEsT").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_extraQueryWhitespace_normalizesFeedbackAndMatching() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"));

        assertEquals("1 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Best Man",
                new SearchRoleCommand(EVENT_ID, " \tBest  \n Man ").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_multipleMatchingRoles_returnsOneRowWithAllRolesSorted() throws CommandException {
        ModelManager model = modelWithParticipations(
                participation(EVENT, ALICE, "Usher", "Best Man", "Assistant Best Man"));

        assertEquals("1 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Assistant Best Man, Best Man, Usher",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_matchingRolesInAnotherEvent_excludesOtherEvent() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Usher"),
                participation(OTHER_EVENT, ALICE, "Best Man"), participation(EVENT, BOB, "Best Man"));

        assertEquals("1 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P2 — Bob Lim — Best Man",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_emptyEvent_returnsNoMatchesMessage() throws CommandException {
        assertEquals("No participants with roles matching \"Best Man\" were found in Jordan's Wedding.",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(modelWithParticipations()).getFeedbackToUser());
    }

    @Test
    public void execute_noMatchingRoles_returnsNoMatchesMessage() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Usher"),
                participation(EVENT, BOB));

        assertEquals("No participants with roles matching \"Best Man\" were found in Jordan's Wedding.",
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

        assertEquals("2 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P1 — Michael — Best Man\nP27 — Michael — Best Man",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_activeFilters_doesNotLimitSearchOrChangeFilters() throws CommandException {
        ModelManager model = modelWithParticipations(participation(EVENT, ALICE, "Best Man"),
                participation(EVENT, BOB, "Best Man"));
        model.updateFilteredEventList(event -> event.equals(OTHER_EVENT));
        model.updateFilteredPersonList(person -> person.equals(BOB));

        assertEquals("2 participants with roles matching \"Best Man\" found in Jordan's Wedding.\n"
                + "P1 — Alice Tan — Best Man\nP2 — Bob Lim — Best Man",
                new SearchRoleCommand(EVENT_ID, "Best Man").execute(model).getFeedbackToUser());
        assertEquals(List.of(OTHER_EVENT), model.getFilteredEventList());
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
