
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
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

    @Test
    public void execute_attendanceRolesAndTags_formatsEveryCombination() throws Exception {
        for (boolean present : new boolean[] {false, true}) {
            for (boolean hasRoles : new boolean[] {false, true}) {
                for (boolean hasTags : new boolean[] {false, true}) {
                    AddressBook people = new AddressBook();
                    people.addPerson(ALICE);
                    EventList events = new EventList();
                    events.addEvent(EVENT);
                    Set<ParticipationRole> roles = hasRoles
                            ? Set.of(new ParticipationRole("Host"), new ParticipationRole("Best Man")) : Set.of();
                    Set<Tag> tags = hasTags ? Set.of(new Tag("vip"), new Tag("friend")) : Set.of();
                    Participation participation = new Participation(EVENT, ALICE, present, tags, roles);
                    ModelManager testModel = new ModelManager(people, new UserPrefs(), events, () ->
                            Set.of(participation));
                    String row = (present ? "[Y]" : "[N]") + "P1-Alice Tan-roles:"
                            + (hasRoles ? "Best Man, Host" : "No role")
                            + "（Tags:" + (hasTags ? "friend, vip" : "None") + "）";
                    String expected = "1 participants found in Workshop.\n" + row;
                    assertEquals(expected, new ListParticipantsCommand(EVENT_ID)
                            .execute(testModel).getFeedbackToUser());
                    assertEquals("Event Details\nEvent ID: E1\nName: Workshop\nCapacity: 10\n"
                            + "Description: Learn Java\n\n" + expected,
                            new ViewEventCommand(EVENT_ID).execute(testModel).getFeedbackToUser());
                    assertEquals(Set.of(participation), testModel.getParticipations().getParticipations());
                }
            }
        }
    }

    @Test
    public void execute_personTags_areNotUsedAsEventTags() throws Exception {
        var person = new PersonBuilder().withId("P3").withName("Carol").withTags("personal").build();
        model.addPerson(person);
        model.addParticipation(EVENT, person);
        model.setTags(EVENT, person, Set.of(new Tag("eventOnly")));
        assertEquals("1 participants found in Workshop.\n[N]P3-Carol-roles:No role（Tags:eventOnly）",
                new ListParticipantsCommand(EVENT_ID).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_hiddenEvents_preservesFiltersAndAttendance() throws Exception {
        model.addParticipation(EVENT, BOB);
        model.addParticipation(EVENT, ALICE);
        model.markPresent(EVENT_ID, ALICE.getId());
        model.updateFilteredEventList(unused -> false);
        model.updateFilteredPersonList(unused -> false);
        assertEquals("2 participants found in Workshop.\n[Y]P1-Alice Tan-roles:No role（Tags:None）\n"
                + "[N]P2-Bob Lim-roles:No role（Tags:None）",
                new ListParticipantsCommand(EVENT_ID).execute(model).getFeedbackToUser());
        assertTrue(model.getFilteredEventList().isEmpty());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListParticipantsCommand(null));
        assertThrows(NullPointerException.class, () -> new ListParticipantsCommand(EVENT_ID).execute(null));
    }

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addEvent(EVENT);
        model.addEvent(OTHER_EVENT);
        model.addPerson(ALICE);
        model.addPerson(BOB);
    }

    @Test
    public void execute_eventWithParticipants_listsParticipants()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);
        model.addParticipation(EVENT, BOB);

        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        String feedback = command.execute(model).getFeedbackToUser();

        assertTrue(feedback.contains("2 participants found"));
        assertTrue(feedback.contains("Workshop"));
        assertTrue(feedback.contains("P1"));
        assertTrue(feedback.contains("Alice Tan"));
        assertTrue(feedback.contains("P2"));
        assertTrue(feedback.contains("Bob Lim"));
    }

    @Test
    public void execute_emptyEvent_displaysEmptyMessage()
            throws CommandException {
        ListParticipantsCommand command =
                new ListParticipantsCommand(EVENT_ID);

        CommandResult result = command.execute(model);

        assertEquals("No participants found in Workshop.",
                result.getFeedbackToUser());
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
    public void execute_listParticipants_doesNotModifyData()
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
