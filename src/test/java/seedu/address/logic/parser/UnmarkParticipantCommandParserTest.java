package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnmarkParticipantCommand;
import seedu.address.model.event.EventId;
import seedu.address.model.person.PersonId;

public class UnmarkParticipantCommandParserTest {

    private final UnmarkParticipantCommandParser parser = new UnmarkParticipantCommandParser();

    @Test
    public void parse_validArguments_returnsUnmarkParticipantCommand() {
        assertParseSuccess(parser, " E1   P1 ",
                new UnmarkParticipantCommand(new EventId("E1"), new PersonId("P1")));
    }

    @Test
    public void parse_wrongArgumentCount_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                UnmarkParticipantCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "E1", expectedMessage);
        assertParseFailure(parser, "E1 P1 extra", expectedMessage);
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        assertParseFailure(parser, "1 P1", EventId.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidPersonId_throwsParseException() {
        assertParseFailure(parser, "E1 1", PersonId.MESSAGE_CONSTRAINTS);
    }
}
