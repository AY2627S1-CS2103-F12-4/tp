package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Template for find-event parser tests. Add one test at a time after predicate tests are green.
 */
public class FindEventCommandParserTest {
    private FindEventCommandParser parser = new FindEventCommandParser();

    // TODO: Test that blank arguments produce a ParseException with the usage message.
    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindEventCommand() {
        // no leading and trailing whitespaces
        FindEventCommand expectedFindCommand =
                new FindEventCommand(new EventNameContainsKeywordPredicate("Workshop"));
        assertParseSuccess(parser, "Workshop", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Workshop \t", expectedFindCommand);
    }
}
