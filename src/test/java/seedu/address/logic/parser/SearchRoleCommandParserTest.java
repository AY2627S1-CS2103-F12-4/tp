package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ROLE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.model.event.EventId;

/**
 * Tests role search argument parsing, whitespace handling, and informative validation errors.
 */
public class SearchRoleCommandParserTest {
    private static final EventId EVENT_ID = new EventId("E1");
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchRoleCommand.MESSAGE_USAGE);

    private final SearchRoleCommandParser parser = new SearchRoleCommandParser();

    @Test
    public void parse_validSingleWord_returnsCommand() {
        assertParseSuccess(parser, " E1 /role Usher", new SearchRoleCommand(EVENT_ID, "Usher"));
    }

    @Test
    public void parse_otherEventIdWithoutLeadingSpace_preservesEventId() {
        assertParseSuccess(parser, "E27 /role Lead", new SearchRoleCommand(new EventId("E27"), "Lead"));
    }

    @Test
    public void parse_multiwordQuery_preservesCompletePhraseAndCase() {
        assertParseSuccess(parser, " E1 /role bEsT Man", new SearchRoleCommand(EVENT_ID, "bEsT Man"));
    }

    @Test
    public void parse_extraWhitespace_normalizesQuery() {
        assertParseSuccess(parser, "  E1 \t /role \t Best  \n Man \t ",
                new SearchRoleCommand(EVENT_ID, "Best Man"));
    }

    @Test
    public void parse_tabBeforePrefix_recognizesRolePrefix() {
        assertParseSuccess(parser, "\tE1\t/role\tBest Man", new SearchRoleCommand(EVENT_ID, "Best Man"));
    }

    @Test
    public void parse_valueImmediatelyAfterPrefix_acceptsTokenizerSyntax() {
        assertParseSuccess(parser, " E1 /roleBest Man", new SearchRoleCommand(EVENT_ID, "Best Man"));
    }

    @Test
    public void parse_searchTextOutsideRoleConstraints_acceptsQuery() {
        for (String keyword : new String[] {"?", "a".repeat(51), "Assistant's (Lead)"}) {
            assertParseSuccess(parser, " E1 /role " + keyword, new SearchRoleCommand(EVENT_ID, keyword));
        }
    }

    @Test
    public void parse_missingArguments_throwsUsageError() {
        for (String input : new String[] {"", " \t ", " E1", " E1 Best Man", " /role Best Man",
            " /role", " E1 /name Best Man", " E1 /Role Best Man", " E1/role Best Man"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_blankQuery_throwsUsageError() {
        for (String input : new String[] {" E1 /role", " E1 /role \t\n ", " E1 /role \u2003"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_invalidEventId_throwsIdConstraintError() {
        for (String eventId : new String[] {"1", "e1", "P1", "E", "E-1", "E1 P1", "E1 E2"}) {
            assertParseFailure(parser, " " + eventId + " /role Best Man", EventId.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_eventIdAfterRolePrefix_throwsUsageError() {
        assertParseFailure(parser, " /role Best Man E1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefixes_throwsDuplicateFieldError() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ROLE);
        for (String input : new String[] {" E1 /role Best Man /role Usher", " E1 /role Best Man /role Best Man",
            " E1 /role /role Usher", " E1 /role Best Man /role", " E1 /role Best Man\t/role Usher"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
