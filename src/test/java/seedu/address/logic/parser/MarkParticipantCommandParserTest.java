package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.MarkParticipantCommand;
import seedu.address.model.event.EventId;
import seedu.address.model.person.PersonId;

public class MarkParticipantCommandParserTest {

    private final MarkParticipantCommandParser parser = new MarkParticipantCommandParser();

    @Test
    public void parse_validArguments_returnsMarkParticipantCommand() {
        assertParseSuccess(parser, " E1   P1 ",
                new MarkParticipantCommand(new EventId("E1"), new PersonId("P1")));
    }

    @Test
    public void parse_wrongArgumentCount_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                MarkParticipantCommand.MESSAGE_USAGE);

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
