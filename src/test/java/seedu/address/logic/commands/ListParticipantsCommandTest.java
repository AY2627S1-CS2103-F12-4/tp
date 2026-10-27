package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests for ListParticipantsCommand.
 */
public class ListParticipantsCommandTest {

    private static final EventId EVENT_ID = new EventId("E1");
    private static final EventId OTHER_EVENT_ID = new EventId("E2");

    private static final Event EVENT =
            new Event(EVENT_ID, "Workshop", 10, "Learn Java");

    private static final Event OTHER_EVENT =
            new Event(OTHER_EVENT_ID, "Concert", 20, "Live music");

    private static final Person ALICE = new PersonBuilder()
            .withId("P1")
            .withName("Alice Tan")
            .build();

    private static final Person BOB = new PersonBuilder()
            .withId("P2")
            .withName("Bob Lim")
            .build();

    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addEvent(EVENT);
        model.addEvent(OTHER_EVENT);
        model.addPerson(ALICE);
        model.addPerson(BOB);
    }

    @Test
    public void execute_attendanceRolesAndTags_formatsEveryCombination()
            throws Exception {
        for (boolean present : new boolean[] {false, true}) {
            for (boolean hasRoles : new boolean[] {false, true}) {
                for (boolean hasTags : new boolean[] {false, true}) {
                    AddressBook people = new AddressBook();
                    people.addPerson(ALICE);

                    EventList events = new EventList();
                    events.addEvent(EVENT);

                    Set<ParticipationRole> roles = hasRoles
                            ? Set.of(new ParticipationRole("Host"), new ParticipationRole("Best Man"))
                            : Set.of();

                    Set<Tag> tags = hasTags
                            ? Set.of(new Tag("vip"), new Tag("friend"))
                            : Set.of();

                    Participation participation = new Participation(
                            EVENT,
                            ALICE,
                            present,
                            tags,
                            roles);

                    ModelManager testModel = new ModelManager(
                            people,
                            new UserPrefs(),
                            events, () -> Set.of(participation));

                    String row = (present ? "[Y]" : "[N]")
                            + "P1-Alice Tan-roles:"
                            + (hasRoles
                            ? "Best Man, Host"
                            : "No role")
                            + "（Tags:"
                            + (hasTags ? "friend, vip" : "None")
                            + "）";

                    String expected =
                            "1 participants found in Workshop.\n" + row;

                    CommandResult result =
                            new ListParticipantsCommand(EVENT_ID)
                                    .execute(testModel);

                    assertEquals(expected, result.getFeedbackToUser());
                    assertTrue(result.isShowParticipants());
                    assertFalse(result.isShowEvents());
                    assertEquals(
                            EVENT_ID.toString(),
                            testModel.getSelectedEvent()
                                    .orElseThrow()
                                    .getId());

                    assertEquals(
                            "Event Details\n"
                                    + "Event ID: E1\n"
                                    + "Name: Workshop\n"
                                    + "Capacity: 10\n"
                                    + "Description: Learn Java\n\n"
                                    + expected,
                            new ViewEventCommand(EVENT_ID)
                                    .execute(testModel)
                                    .getFeedbackToUser());

                    assertEquals(
                            Set.of(participation),
                            testModel.getParticipations()
                                    .getParticipations());
                }
            }
        }
    }

    @Test
    public void execute_personTags_areNotUsedAsEventTags()
            throws Exception {
        Person person = new PersonBuilder()
                .withId("P3")
                .withName("Carol")
                .withTags("personal")
                .build();

        model.addPerson(person);
        model.addParticipation(EVENT, person);
        model.setTags(
                EVENT,
                person,
                Set.of(new Tag("eventOnly")));

        assertEquals(
                "1 participants found in Workshop.\n"
                        + "[N]P3-Carol-roles:No role"
                        + "（Tags:eventOnly）",
                new ListParticipantsCommand(EVENT_ID)
                        .execute(model)
                        .getFeedbackToUser());
    }

    @Test
    public void execute_hiddenEvents_restoresEventsAndPreservesPeopleFilterAndAttendance()
            throws Exception {
        model.addParticipation(EVENT, BOB);
        model.addParticipation(EVENT, ALICE);
        model.markPresent(EVENT_ID, ALICE.getId());
        model.updateFilteredEventList(unused -> false);
        model.updateFilteredPersonList(unused -> false);

        CommandResult result =
                new ListParticipantsCommand(EVENT_ID).execute(model);

        assertEquals(
                "2 participants found in Workshop.\n"
                        + "[Y]P1-Alice Tan-roles:No role"
                        + "（Tags:None）\n"
                        + "[N]P2-Bob Lim-roles:No role"
                        + "（Tags:None）",
                result.getFeedbackToUser());

        assertTrue(result.isShowParticipants());
        assertEquals(
                EVENT_ID.toString(),
                model.getSelectedEvent().orElseThrow().getId());
        assertEquals(List.of(EVENT, OTHER_EVENT), model.getFilteredEventList());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void parseAndExecute_activeSearch_clearsSearchAndListsAllTargetParticipants() throws Exception {
        Event archived = new Event(new EventId("E3"), "Archived", 10, "", true);
        model.addEvent(archived);
        model.addParticipation(EVENT_ID, ALICE.getId(), Set.of(new ParticipationRole("Host")), Set.of());
        model.addParticipation(OTHER_EVENT_ID, ALICE.getId(), Set.of(new ParticipationRole("Speaker")),
                Set.of(new Tag("vip")));
        model.addParticipation(OTHER_EVENT, BOB);
        model.markPresent(OTHER_EVENT_ID, ALICE.getId());
        model.updateFilteredPersonList(unused -> false);
        Set<Participation> participations = Set.copyOf(model.getParticipations().getParticipations());
        AddressBookParser parser = new AddressBookParser();

        for (String search : List.of("search-participant Alice /event E1",
                "search-role Host /event E1", "search-participant Alice", "search-role Host",
                "search-participant Missing /include-archive", "search-role Missing /event E1")) {
            parser.parseCommand(search).execute(model);
            assertTrue(model.getParticipantSearchKeyword().isPresent());

            CommandResult result = parser.parseCommand("list-participants E2").execute(model);

            assertTrue(result.isShowParticipants());
            assertTrue(result.getFeedbackToUser().contains("2 participants found in Concert."));
            assertTrue(result.getFeedbackToUser().contains("[Y]P1-Alice Tan-roles:Speaker"));
            assertTrue(result.getFeedbackToUser().contains("P2-Bob Lim"));
            assertEquals(OTHER_EVENT, model.getSelectedEvent().orElseThrow());
            assertEquals(List.of(EVENT, OTHER_EVENT), model.getFilteredEventList());
            assertTrue(model.getParticipantSearchKeyword().isEmpty());
            assertTrue(model.getParticipantSearchEvent().isEmpty());
            assertTrue(model.getParticipantSearchResults().isEmpty());
            assertFalse(model.isParticipantSearchIncludingArchived());
            assertTrue(model.getFilteredPersonList().isEmpty());
            assertEquals(participations, model.getParticipations().getParticipations());
        }

        model.markPresent(EVENT_ID, ALICE.getId());
        assertEquals(List.of(EVENT, OTHER_EVENT), model.getFilteredEventList());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        assertTrue(model.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void execute_emptyEventAfterSearch_clearsSearchAndSelectsEmptyEvent() throws Exception {
        model.addParticipation(EVENT, ALICE);
        model.updateParticipantSearch(EVENT_ID, "Alice");

        CommandResult result = new ListParticipantsCommand(OTHER_EVENT_ID).execute(model);

        assertEquals("No participants found in Concert.", result.getFeedbackToUser());
        assertTrue(result.isShowParticipants());
        assertEquals(OTHER_EVENT, model.getSelectedEvent().orElseThrow());
        assertEquals(List.of(EVENT, OTHER_EVENT), model.getFilteredEventList());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertTrue(model.getParticipantSearchResults().isEmpty());
    }

    @Test
    public void execute_archivedTargetAfterSearch_clearsSearchAndIncludesArchivedEvents() throws Exception {
        Event archived = OTHER_EVENT.withArchived(true);
        model.updateEvent(archived);
        model.addParticipation(EVENT, ALICE);
        model.addParticipation(archived, BOB);
        model.updateParticipantSearch(EVENT_ID, "Alice");
        Set<Participation> participations = Set.copyOf(model.getParticipations().getParticipations());

        CommandResult result = new ListParticipantsCommand(OTHER_EVENT_ID).execute(model);

        assertTrue(result.getFeedbackToUser().contains("Bob Lim"));
        assertTrue(result.isShowParticipants());
        assertEquals(archived, model.getSelectedEvent().orElseThrow());
        assertEquals(List.of(EVENT, archived), model.getFilteredEventList());
        assertTrue(model.getParticipantSearchKeyword().isEmpty());
        assertTrue(model.getParticipantSearchEvent().isEmpty());
        assertTrue(model.getParticipantSearchResults().isEmpty());
        assertFalse(model.isParticipantSearchIncludingArchived());
        assertEquals(participations, model.getParticipations().getParticipations());
    }

    @Test
    public void parseAndExecute_nonexistentEvent_preservesSearchSelectionAndArchiveVisibility() throws Exception {
        Event archived = EVENT.withArchived(true);
        model.updateEvent(archived);
        model.addParticipation(EVENT_ID, ALICE.getId(), Set.of(new ParticipationRole("Host")), Set.of());
        AddressBookParser parser = new AddressBookParser();

        for (String search : List.of("search-participant Alice /event E1 /include-archive",
                "search-role Host /event E1 /include-archive")) {
            parser.parseCommand(search).execute(model);
            model.selectEvent(EVENT_ID);
            List<Participation> results = List.copyOf(model.getParticipantSearchResults());
            String keyword = model.getParticipantSearchKeyword().orElseThrow();

            CommandException exception = assertThrows(CommandException.class, () ->
                    parser.parseCommand("list-participants E99").execute(model));

            assertEquals("Event E99 does not exist.", exception.getMessage());
            assertEquals(archived, model.getSelectedEvent().orElseThrow());
            assertEquals(List.of(archived), model.getFilteredEventList());
            assertEquals(keyword, model.getParticipantSearchKeyword().orElseThrow());
            assertEquals(archived, model.getParticipantSearchEvent().orElseThrow());
            assertEquals(results, model.getParticipantSearchResults());
            assertTrue(model.isParticipantSearchIncludingArchived());
        }
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new ListParticipantsCommand(null));

        assertThrows(NullPointerException.class, () ->
                new ListParticipantsCommand(EVENT_ID).execute(null));
    }

    @Test
    public void execute_eventWithParticipants_listsAndSelectsEvent()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);
        model.addParticipation(EVENT, BOB);

        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        CommandResult result = command.execute(model);
        String feedback = result.getFeedbackToUser();

        assertTrue(feedback.contains("2 participants found"));
        assertTrue(feedback.contains("Workshop"));
        assertTrue(feedback.contains("P1"));
        assertTrue(feedback.contains("Alice Tan"));
        assertTrue(feedback.contains("P2"));
        assertTrue(feedback.contains("Bob Lim"));

        assertTrue(result.isShowParticipants());
        assertFalse(result.isShowEvents());
        assertEquals(
                EVENT_ID.toString(),
                model.getSelectedEvent().orElseThrow().getId());
    }

    @Test
    public void execute_emptyEvent_displaysEmptyMessageAndSelectsEvent()
            throws CommandException {
        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        CommandResult result = command.execute(model);

        assertEquals(
                "No participants found in Workshop.",
                result.getFeedbackToUser());
        assertTrue(result.isShowParticipants());
        assertFalse(result.isShowEvents());
        assertEquals(
                EVENT_ID.toString(),
                model.getSelectedEvent().orElseThrow().getId());
    }

    @Test
    public void execute_nonexistentEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E99");

        ListParticipantsCommand command =
                new ListParticipantsCommand(missingEventId);

        CommandException exception = assertThrows(
                CommandException.class, () -> command.execute(model));

        String expectedMessage = String.format(
                ListParticipantsCommand.MESSAGE_EVENT_NOT_FOUND,
                missingEventId);

        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void execute_participantsInDifferentEvents_listsOnlyMatchingEvent()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);
        model.addParticipation(OTHER_EVENT, BOB);

        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        String feedback = command.execute(model).getFeedbackToUser();

        assertTrue(feedback.contains("Alice Tan"));
        assertFalse(feedback.contains("Bob Lim"));
        assertTrue(feedback.contains("1 participants found"));
    }

    @Test
    public void execute_listParticipants_doesNotModifyParticipationData()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);

        int before = model.getParticipations()
                .getParticipations().size();

        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        command.execute(model);

        int after = model.getParticipations()
                .getParticipations().size();

        assertEquals(before, after);
    }
}
