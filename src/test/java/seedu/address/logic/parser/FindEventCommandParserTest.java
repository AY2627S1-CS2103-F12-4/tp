package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindEventCommand;
import seedu.address.model.event.EventNameContainsKeywordPredicate;

/**
 * Tests event-name keywords and the optional archive inclusion flag.
 */
public class FindEventCommandParserTest {
    private FindEventCommandParser parser = new FindEventCommandParser();

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

    @Test
    public void parse_multiwordKeyword_preservesPhrase() {
        assertParseSuccess(parser, "  Java Workshop  ",
                new FindEventCommand(new EventNameContainsKeywordPredicate("Java Workshop")));
        assertParseSuccess(parser, "  Java  Workshop  ",
                new FindEventCommand(new EventNameContainsKeywordPredicate("Java  Workshop")));
    }

    @Test
    public void parse_trailingArchiveFlag_excludesFlagFromKeyword() {
        FindEventCommand expected = new FindEventCommand(
                new EventNameContainsKeywordPredicate("Java Workshop"), true);
        assertParseSuccess(parser, "Java Workshop /include-archive", expected);
        assertParseSuccess(parser, " \n Java Workshop \t /include-archive \t", expected);
    }

    @Test
    public void parse_slashWithinKeyword_preservesKeyword() {
        assertParseSuccess(parser, "Java/Kotlin /include-archive",
                new FindEventCommand(new EventNameContainsKeywordPredicate("Java/Kotlin"), true));
    }

    @Test
    public void parse_invalidOptions_throwsParseException() {
        for (String invalid : List.of("/include-archive", "/include-archive Workshop",
                "Workshop /include-archive Party", "Workshop /include-archive /include-archive",
                "Workshop /unknown", "Workshop /unknown /include-archive",
                "Workshop /include-archive=true", "Workshop /INCLUDE-ARCHIVE")) {
            assertParseFailure(parser, invalid,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindEventCommand.MESSAGE_USAGE));
        }
    }
}
