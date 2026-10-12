
package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListParticipantsCommand;
import seedu.address.model.event.EventId;

public class ListParticipantsCommandParserTest {

    private final ListParticipantsCommandParser parser =
            new ListParticipantsCommandParser();

    @Test
    public void parse_validEventId_returnsListParticipantsCommand() {
        assertInstanceOf(ListParticipantsCommand.class,
                assertDoesNotThrow(() -> parser.parse(" E1 ")));
    }

    @Test
    public void parse_missingEventId_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT,
                ListParticipantsCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        assertParseFailure(parser, "abc",
                EventId.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1",
                EventId.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String expectedMessage = String.format(
                MESSAGE_INVALID_COMMAND_FORMAT,
                ListParticipantsCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "E1 E2", expectedMessage);
        assertParseFailure(parser, "E1 extra", expectedMessage);
    }
}
