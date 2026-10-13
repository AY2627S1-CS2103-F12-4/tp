
package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
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
