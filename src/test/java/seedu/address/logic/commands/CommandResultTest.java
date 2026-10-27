package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CommandResultTest {

    @Test
    public void equals() {
        CommandResult commandResult =
                new CommandResult("feedback");

        assertTrue(commandResult.equals(
                new CommandResult("feedback")));
        assertTrue(commandResult.equals(
                new CommandResult("feedback", false, false)));
        assertTrue(commandResult.equals(commandResult));

        assertFalse(commandResult.equals(null));
        assertFalse(commandResult.equals(0.5f));
        assertFalse(commandResult.equals(
                new CommandResult("different")));
        assertFalse(commandResult.equals(
                new CommandResult("feedback", true, false)));
        assertFalse(commandResult.equals(
                new CommandResult("feedback", false, true)));
        assertFalse(commandResult.equals(
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        true,
                        false,
                        false)));
        assertFalse(commandResult.equals(
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        true,
                        false)));
        assertFalse(commandResult.equals(
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        false,
                        true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult =
                new CommandResult("feedback");

        assertEquals(
                commandResult.hashCode(),
                new CommandResult("feedback").hashCode());

        assertNotEquals(
                commandResult.hashCode(),
                new CommandResult("different").hashCode());

        assertNotEquals(
                commandResult.hashCode(),
                new CommandResult(
                        "feedback",
                        true,
                        false).hashCode());

        assertNotEquals(
                commandResult.hashCode(),
                new CommandResult(
                        "feedback",
                        false,
                        true).hashCode());

        assertNotEquals(
                commandResult.hashCode(),
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        false,
                        true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult =
                new CommandResult("feedback");

        String expected =
                CommandResult.class.getCanonicalName()
                        + "{feedbackToUser="
                        + commandResult.getFeedbackToUser()
                        + ", shouldShowHelp="
                        + commandResult.shouldShowHelp()
                        + ", shouldExit="
                        + commandResult.shouldExit()
                        + ", shouldShowEvents="
                        + commandResult.shouldShowEvents()
                        + ", shouldShowParticipants="
                        + commandResult.shouldShowParticipants()
                        + ", shouldShowPeople="
                        + commandResult.shouldShowPeople()
                        + "}";

        assertEquals(expected, commandResult.toString());
    }

    @Test
    public void constructor_eventResults_requestsEventView() {
        CommandResult eventResult =
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        true);

        assertTrue(eventResult.shouldShowEvents());
        assertFalse(eventResult.shouldShowParticipants());
        assertFalse(eventResult.shouldShowPeople());
        assertNotEquals(
                new CommandResult("feedback"),
                eventResult);
        assertEquals(
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        true),
                eventResult);
    }

    @Test
    public void constructor_participantResults_requestsParticipantView() {
        CommandResult result =
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        true);

        CommandResult copy =
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        true);

        assertTrue(result.shouldShowParticipants());
        assertFalse(result.shouldShowEvents());
        assertFalse(result.shouldShowPeople());
        assertFalse(result.shouldShowHelp());
        assertFalse(result.shouldExit());
        assertEquals(result, copy);
        assertEquals(result.hashCode(), copy.hashCode());
        assertNotEquals(
                new CommandResult("feedback"),
                result);
    }

    @Test
    public void constructor_peopleResults_requestsPeopleView() {
        CommandResult result =
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        false,
                        true);

        CommandResult copy =
                new CommandResult(
                        "feedback",
                        false,
                        false,
                        false,
                        false,
                        true);

        assertTrue(result.shouldShowPeople());
        assertFalse(result.shouldShowEvents());
        assertFalse(result.shouldShowParticipants());
        assertFalse(result.shouldShowHelp());
        assertFalse(result.shouldExit());
        assertEquals(result, copy);
        assertEquals(result.hashCode(), copy.hashCode());
        assertNotEquals(
                new CommandResult("feedback"),
                result);
    }
}
