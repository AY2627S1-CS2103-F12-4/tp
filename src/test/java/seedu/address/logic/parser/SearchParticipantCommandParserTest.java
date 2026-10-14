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
    public void parse_validInput_preservesSearchPhrase() throws Exception {
        for (String keyword : List.of("Michael", "mIcHaEl Tan", "Michael  Tan", "O'Neil", "[", "a/b")) {
            assertEquals(new SearchParticipantCommand(new EventId("E1"), keyword),
                    parser.parse("E1 /name " + keyword));
        }
    }

    @Test
    public void parse_surroundingWhitespace_success() throws Exception {
        SearchParticipantCommand expected = new SearchParticipantCommand(new EventId("E2"), "Michael Tan");
        assertEquals(expected, parser.parse("  E2   /name   Michael Tan  "));
        assertEquals(expected, parser.parse("\tE2\t/name\tMichael Tan\t"));
    }

    @Test
    public void parse_missingOrMalformedArguments_throwsParseException() {
        for (String args : List.of("", " ", "E1", "E1 Michael", "/name Michael", "E1 /name",
                "E1 /name   ", "E1 /name \u2003", "E1 n/Michael", "E1 /Name Michael", "E1/name Michael",
                "E1 /nameMichael", "E1 extra /name Michael", "E1 2 /name Michael")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchParticipantCommand.MESSAGE_USAGE),
                    exception.getMessage(), args);
        }
    }

    @Test
    public void parse_repeatedNamePrefix_throwsParseException() {
        for (String args : List.of("E1 /name Michael /name Tan", "E1 /name /name Michael",
                "E1 /name Michael /name", "E1 /name Michael\t/name\tTan")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }

    @Test
    public void parse_invalidEventId_throwsParseException() {
        for (String eventId : List.of("1", "0", "-1", "+1", "1.5", "P1", "e1", "E", "E-1", "E1x")) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parse(eventId + " /name Michael"), eventId);
            assertEquals(EventId.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void parse_validEventIdFormats_preservesExactId() throws Exception {
        for (String eventId : List.of("E1", "E001", "E0", "E2147483648")) {
            assertEquals(new SearchParticipantCommand(new EventId(eventId), "Michael"),
                    parser.parse(eventId + " /name Michael"));
        }
    }

    @Test
    public void parseCommand_searchParticipant_routesToParser() throws Exception {
        assertEquals(new SearchParticipantCommand(new EventId("E1"), "Michael Tan"),
                new AddressBookParser().parseCommand("search-participant E1 /name Michael Tan"));
    }

    @Test
    public void parseCommand_invalidSearchParticipant_propagatesParseException() {
        assertThrows(ParseException.class, () ->
                new AddressBookParser().parseCommand("search-participant E1 /name"));
    }
}
