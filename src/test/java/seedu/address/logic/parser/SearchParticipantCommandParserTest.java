package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

public class SearchParticipantCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchParticipantCommand.MESSAGE_USAGE);

    private final SearchParticipantCommandParser parser = new SearchParticipantCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_preservesCaseAndLiteralCharacters() throws Exception {
        for (String keyword : List.of("Michael", "mIcHaEl Tan", "Michael  Tan", "O'Neil", "[", "a/b",
                "Name/include-archive")) {
            assertEquals(new SearchParticipantCommand(new EventId("E1"), keyword),
                    parser.parse(keyword + " /event E1"));
            assertEquals(new SearchParticipantCommand(keyword), parser.parse(keyword));
            assertEquals(new SearchParticipantCommand(keyword, true), parser.parse(keyword + " /include-archive"));
        }
    }

    @Test
    public void parse_surroundingWhitespace_success() throws Exception {
        SearchParticipantCommand expected = new SearchParticipantCommand(new EventId("E2"), "Michael Tan");
        assertEquals(expected, parser.parse("  Michael  Tan   /event   E2  "));
        assertEquals(expected, parser.parse("\tMichael\tTan\t/event\tE2\t"));
        assertEquals(new SearchParticipantCommand("Michael Tan"), parser.parse("\u2003Michael\u2003Tan\u2003"));
    }

    @Test
    public void parse_blankQuery_throwsUsageError() {
        for (String args : SearchParserTestData.getBlankQueries()) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(MESSAGE_INVALID_FORMAT, exception.getMessage(), args);
        }
    }

    @Test
    public void parse_malformedArguments_throwsUsageError() {
        for (String args : SearchParserTestData.getMalformedArguments("Michael")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(MESSAGE_INVALID_FORMAT, exception.getMessage(), args);
        }
    }

    @Test
    public void parse_repeatedEventPrefix_throwsUsageError() {
        for (String args : SearchParserTestData.getRepeatedEventArguments("Michael")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(MESSAGE_INVALID_FORMAT, exception.getMessage(), args);
        }
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        for (String eventId : SearchParserTestData.getInvalidEventIds()) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parse("Michael /event " + eventId), eventId);
            assertEquals(EventId.MESSAGE_CONSTRAINTS, exception.getMessage());
            ParseException flaggedException = assertThrows(ParseException.class, () ->
                    parser.parse("Michael /event " + eventId + " /include-archive"), eventId);
            assertEquals(EventId.MESSAGE_CONSTRAINTS, flaggedException.getMessage());
        }
    }

    @Test
    public void parse_validEventIdFormats_preservesExactId() throws Exception {
        for (String eventId : List.of("E1", "E001", "E0", "E2147483648")) {
            assertEquals(new SearchParticipantCommand(new EventId(eventId), "Michael"),
                    parser.parse("Michael /event " + eventId));
            assertEquals(new SearchParticipantCommand(new EventId(eventId), "Michael", true),
                    parser.parse("Michael /event " + eventId + " /include-archive"));
        }
    }

    @Test
    public void parseCommand_searchParticipant_routesToParser() throws Exception {
        assertEquals(new SearchParticipantCommand(new EventId("E1"), "Michael Tan"),
                new AddressBookParser().parseCommand("search-participant Michael Tan /event E1"));
        assertEquals(new SearchParticipantCommand("Michael Tan"),
                new AddressBookParser().parseCommand("search-participant Michael Tan"));
    }

    @Test
    public void parseCommand_invalidSearchParticipant_propagatesParseException() {
        assertThrows(ParseException.class, () ->
                new AddressBookParser().parseCommand("search-participant Michael /event"));
    }

    @Test
    public void parse_archiveFlag_normalizesWhitespaceAndPreservesScopeAndQuery() throws Exception {
        assertEquals(new SearchParticipantCommand("mIcHaEl Tan", true),
                parser.parse("\u2003mIcHaEl\tTan\u00a0/include-archive\u2003"));
        assertEquals(new SearchParticipantCommand(new EventId("E2"), "mIcHaEl Tan", true),
                parser.parse("\t mIcHaEl\tTan /event\u2003E2\t/include-archive \n"));
    }

    @Test
    public void parse_malformedArchiveFlag_throwsUsageError() {
        for (String args : SearchParserTestData.getMalformedArchiveArguments("Michael")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(MESSAGE_INVALID_FORMAT, exception.getMessage(), args);
        }
    }

    @Test
    public void parseCommand_archiveFlag_routesGlobalAndScopedSearches() throws Exception {
        AddressBookParser commandParser = new AddressBookParser();
        assertEquals(new SearchParticipantCommand("Michael Tan", true),
                commandParser.parseCommand("search-participant Michael Tan /include-archive"));
        assertEquals(new SearchParticipantCommand(new EventId("E1"), "Michael Tan", true),
                commandParser.parseCommand("search-participant Michael Tan /event E1 /include-archive"));
        assertThrows(ParseException.class, () ->
                commandParser.parseCommand("search-participant Michael /include-archive /event E1"));
    }
}
