package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
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
        assertParseSuccess(parser, "Usher /event E1", new SearchRoleCommand(EVENT_ID, "Usher"));
        assertParseSuccess(parser, "Usher", new SearchRoleCommand("Usher"));
    }

    @Test
    public void parse_otherEventIdWithoutLeadingSpace_preservesEventId() {
        assertParseSuccess(parser, "Lead /event E27", new SearchRoleCommand(new EventId("E27"), "Lead"));
    }

    @Test
    public void parse_multiwordQuery_preservesCompletePhraseAndCase() {
        assertParseSuccess(parser, " bEsT Man /event E1", new SearchRoleCommand(EVENT_ID, "bEsT Man"));
        assertParseSuccess(parser, " bEsT Man ", new SearchRoleCommand("bEsT Man"));
    }

    @Test
    public void parse_extraWhitespace_normalizesQuery() {
        assertParseSuccess(parser, "  Best \t\n Man \t /event \t E1 \t ",
                new SearchRoleCommand(EVENT_ID, "Best Man"));
        assertParseSuccess(parser, "\u2003Best\u00a0Man\u2003", new SearchRoleCommand("Best Man"));
    }

    @Test
    public void parse_tabBeforePrefix_recognizesEventPrefix() {
        assertParseSuccess(parser, "\tBest Man\t/event\tE1", new SearchRoleCommand(EVENT_ID, "Best Man"));
    }

    @Test
    public void parse_valueImmediatelyAfterPrefix_throwsUsageError() {
        assertParseFailure(parser, "Best Man /eventE1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_searchTextOutsideRoleConstraints_acceptsQuery() {
        for (String keyword : new String[] {"?", "a".repeat(51), "Assistant's (Lead)", "Host/Coordinator", "E1",
            "Host/include-archive"}) {
            assertParseSuccess(parser, keyword + " /event E1", new SearchRoleCommand(EVENT_ID, keyword));
            assertParseSuccess(parser, keyword, new SearchRoleCommand(keyword));
            assertParseSuccess(parser, keyword + " /include-archive", new SearchRoleCommand(keyword, true));
        }
    }

    @Test
    public void parse_missingArguments_throwsUsageError() {
        for (String input : new String[] {"", " \t ", "/event E1", "Best Man /event", "/event",
            "Best Man /name Alice", "Best Man /foo E1", "Best Man /Event E1", "Best Man /event E1 extra",
            "Best Man /event /event", "Best Man /", "E1 /role Best Man", "E1 /roleBest Man"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_blankQuery_throwsUsageError() {
        for (String input : new String[] {"\u2003", "\u00a0", " /event E1", " \t\n /event E1"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_invalidEventId_throwsIdConstraintError() {
        for (String eventId : new String[] {"1", "0", "e1", "P1", "E", "E-1", "E1x", "+1", "1.5"}) {
            assertParseFailure(parser, "Best Man /event " + eventId, EventId.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "Best Man /event " + eventId + " /include-archive", EventId.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_eventScopeBeforeQuery_throwsUsageError() {
        assertParseFailure(parser, " /event E1 Best Man", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicatePrefixes_throwsUsageError() {
        for (String input : new String[] {"Best Man /event E1 /event E2", "Best Man /event E1 /event E1",
            "Best Man /event /event E1", "Best Man /event E1 /event", "Best Man /event E1\t/event E2"}) {
            assertParseFailure(parser, input, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_validEventIdFormats_preservesExactId() {
        for (String eventId : new String[] {"E0", "E001", "E2147483648"}) {
            assertParseSuccess(parser, "Best Man /event " + eventId,
                    new SearchRoleCommand(new EventId(eventId), "Best Man"));
            assertParseSuccess(parser, "Best Man /event " + eventId + " /include-archive",
                    new SearchRoleCommand(new EventId(eventId), "Best Man", true));
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_archiveFlag_normalizesWhitespaceAndPreservesScopeAndQuery() {
        assertParseSuccess(parser, "\u2003bEsT\tMan\u00a0/include-archive\u2003",
                new SearchRoleCommand("bEsT Man", true));
        assertParseSuccess(parser, "\t bEsT\tMan /event\u2003E1\t/include-archive \n",
                new SearchRoleCommand(EVENT_ID, "bEsT Man", true));
    }

    @Test
    public void parse_malformedArchiveFlag_throwsUsageError() {
        for (String args : new String[] {"/include-archive", "/event E1 /include-archive", "/include-archive Best Man",
            "Best Man /include-archive /event E1", "Best Man /include-archive extra", "Best Man /include-archive true",
            "Best Man /include-archive /include-archive", "Best Man /event E1 /include-archive /include-archive",
            "Best Man /include-archivex", "Best Man /Include-Archive", "Best Man /include-archive=true",
            "Best Man /event /include-archive", "Best Man /event E1 extra /include-archive",
            "Best Man /unknown /include-archive", "Best Man /event E1 /event E2 /include-archive"}) {
            assertParseFailure(parser, args, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parseCommand_archiveFlag_routesGlobalAndScopedSearches() throws Exception {
        AddressBookParser commandParser = new AddressBookParser();
        assertEquals(new SearchRoleCommand("Best Man", true),
                commandParser.parseCommand("search-role Best Man /include-archive"));
        assertEquals(new SearchRoleCommand(EVENT_ID, "Best Man", true),
                commandParser.parseCommand("search-role Best Man /event E1 /include-archive"));
        assertThrows(ParseException.class, () ->
                commandParser.parseCommand("search-role Best Man /include-archive /event E1"));
    }
}
