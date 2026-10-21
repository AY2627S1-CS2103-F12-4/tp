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
    private final SearchParticipantCommandParser parser = new SearchParticipantCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_preservesCaseAndLiteralCharacters() throws Exception {
        for (String keyword : List.of("Michael", "mIcHaEl Tan", "Michael  Tan", "O'Neil", "[", "a/b")) {
            assertEquals(new SearchParticipantCommand(new EventId("E1"), keyword),
                    parser.parse(keyword + " /event E1"));
            assertEquals(new SearchParticipantCommand(keyword), parser.parse(keyword));
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
    public void parse_missingOrMalformedArguments_throwsParseException() {
        for (String args : List.of("", " ", "\u2003", "\u00a0", "/event E1", "Michael /event",
                "Michael /event \u2003", "Michael /event /event", "Michael /eventE1", "/event E1 Michael",
                "Michael /event E1 extra", "Michael /Event E1", "Michael /name Tan", "Michael /foo E1",
                "E1 /name Michael", "Michael /role Speaker", "Michael /")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchParticipantCommand.MESSAGE_USAGE),
                    exception.getMessage(), args);
        }
    }

    @Test
    public void parse_repeatedEventPrefix_throwsParseException() {
        for (String args : List.of("Michael /event E1 /event E2", "Michael /event /event E1",
                "Michael /event E1 /event", "Michael /event E1\t/event\tE2")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        for (String eventId : List.of("1", "0", "-1", "+1", "1.5", "P1", "e1", "E", "E-1", "E1x")) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parse("Michael /event " + eventId), eventId);
            assertEquals(EventId.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void parse_validEventIdFormats_preservesExactId() throws Exception {
        for (String eventId : List.of("E1", "E001", "E0", "E2147483648")) {
            assertEquals(new SearchParticipantCommand(new EventId(eventId), "Michael"),
                    parser.parse("Michael /event " + eventId));
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
}
