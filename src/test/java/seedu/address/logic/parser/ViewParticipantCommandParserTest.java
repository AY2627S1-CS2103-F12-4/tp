
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ViewParticipantCommandParserTest {

    private final ViewParticipantCommandParser parser =
            new ViewParticipantCommandParser();

    @Test
    public void parse_validArguments_returnsCommand() throws ParseException {
        assertInstanceOf(ViewParticipantCommand.class,
                parser.parse(" E1 P2 "));
    }

    @Test
    public void parse_missingArguments_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT,
                ViewParticipantCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "E1", expectedMessage);
        assertParseFailure(parser, "P2", expectedMessage);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT,
                ViewParticipantCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "E1 P2 extra", expectedMessage);
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("abc P2"));
    }

    @Test
    public void parse_invalidPersonId_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("E1 abc"));
    }
}
