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
import seedu.address.model.person.PersonId;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains tests for ViewParticipantCommand.
 */
public class ViewParticipantCommandTest {

    private static final EventId EVENT_ID = new EventId("E1");
    private static final EventId OTHER_EVENT_ID = new EventId("E2");
    private static final PersonId PERSON_ID = new PersonId("P1");

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
    public void execute_validParticipant_displaysDetailsAndSelectsEvent()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);

        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        CommandResult result = command.execute(model);
        String feedback = result.getFeedbackToUser();

        assertTrue(feedback.contains("Workshop"));
        assertTrue(feedback.contains("P1"));
        assertTrue(feedback.contains("Alice Tan"));
        assertTrue(feedback.contains(ALICE.getPhone().toString()));
        assertTrue(feedback.contains(ALICE.getEmail().toString()));
        assertTrue(feedback.contains(ALICE.getAddress().toString()));
        assertTrue(feedback.contains("Present: No"));

        assertTrue(result.isShowParticipants());
        assertFalse(result.isShowEvents());
        assertEquals(
                EVENT_ID.toString(),
                model.getSelectedEvent().orElseThrow().getId());
    }

    @Test
    public void execute_nonexistentEvent_throwsCommandException() {
        EventId missingEventId = new EventId("E99");

        ViewParticipantCommand command =
                new ViewParticipantCommand(missingEventId, PERSON_ID);

        CommandException exception = assertThrows(
                CommandException.class, () -> command.execute(model));

        String expectedMessage = String.format(
                ViewParticipantCommand.MESSAGE_EVENT_NOT_FOUND,
                missingEventId);

        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void execute_personNotRegistered_throwsCommandException() {
        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        CommandException exception = assertThrows(
                CommandException.class, () -> command.execute(model));

        String expectedMessage = String.format(
                ViewParticipantCommand.MESSAGE_PARTICIPANT_NOT_FOUND,
                EVENT_ID,
                PERSON_ID);

        assertEquals(expectedMessage, exception.getMessage());
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void execute_personRegisteredInOtherEvent_throwsCommandException() {
        model.addParticipation(OTHER_EVENT, ALICE);

        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        assertThrows(
                CommandException.class, () -> command.execute(model));
        assertTrue(model.getSelectedEvent().isEmpty());
    }

    @Test
    public void execute_displaysOnlySelectedParticipant()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);
        model.addParticipation(EVENT, BOB);

        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        String feedback = command.execute(model).getFeedbackToUser();

        assertTrue(feedback.contains("Alice Tan"));
        assertFalse(feedback.contains("Bob Lim"));
    }

    @Test
    public void execute_markedParticipant_displaysPresent()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);
        model.markPresent(EVENT_ID, PERSON_ID);

        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        String feedback = command.execute(model).getFeedbackToUser();

        assertTrue(feedback.contains("Present: Yes"));
    }

    @Test
    public void execute_doesNotModifyParticipationData()
            throws CommandException {
        model.addParticipation(EVENT, ALICE);

        int before = model.getParticipations()
                .getParticipations().size();

        ViewParticipantCommand command =
                new ViewParticipantCommand(EVENT_ID, PERSON_ID);

        command.execute(model);

        int after = model.getParticipations()
                .getParticipations().size();

        assertEquals(before, after);
    }
}
