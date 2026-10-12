package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SearchParticipantCommandParserTest {
    private final SearchParticipantCommandParser parser = new SearchParticipantCommandParser();

    @Test
    public void parse_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validInput_preservesSearchPhrase() throws Exception {
        for (String keyword : List.of("Michael", "mIcHaEl Tan", "Michael  Tan", "O'Neil", "[", "a/b")) {
            assertEquals(new SearchParticipantCommand(Index.fromOneBased(1), keyword),
                    parser.parse("1 /name " + keyword));
        }
    }

    @Test
    public void parse_surroundingWhitespace_success() throws Exception {
        SearchParticipantCommand expected = new SearchParticipantCommand(Index.fromOneBased(2), "Michael Tan");
        assertEquals(expected, parser.parse("  2   /name   Michael Tan  "));
        assertEquals(expected, parser.parse("\t2\t/name\tMichael Tan\t"));
    }

    @Test
    public void parse_missingOrMalformedArguments_throwsParseException() {
        for (String args : List.of("", " ", "1", "1 Michael", "/name Michael", "1 /name",
                "1 /name   ", "1 /name \u2003", "1 n/Michael", "1 /Name Michael", "1/name Michael",
                "1 /nameMichael", "1 extra /name Michael", "1 2 /name Michael")) {
            ParseException exception = assertThrows(ParseException.class, () -> parser.parse(args), args);
            assertEquals(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SearchParticipantCommand.MESSAGE_USAGE),
                    exception.getMessage(), args);
        }
    }

    @Test
    public void parse_repeatedNamePrefix_throwsParseException() {
        for (String args : List.of("1 /name Michael /name Tan", "1 /name /name Michael",
                "1 /name Michael /name", "1 /name Michael\t/name\tTan")) {
            assertThrows(ParseException.class, () -> parser.parse(args), args);
        }
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        for (String index : List.of("0", "-1", "+1", "1.5", "E1", "one", "2147483648")) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parse(index + " /name Michael"), index);
            assertEquals(ParserUtil.MESSAGE_INVALID_INDEX, exception.getMessage());
        }
    }

    @Test
    public void parse_maximumIndex_success() throws Exception {
        assertEquals(new SearchParticipantCommand(Index.fromOneBased(Integer.MAX_VALUE), "Michael"),
                parser.parse("2147483647 /name Michael"));
    }

    @Test
    public void parseCommand_searchParticipant_routesToParser() throws Exception {
        assertEquals(new SearchParticipantCommand(Index.fromOneBased(1), "Michael Tan"),
                new AddressBookParser().parseCommand("search-participant 1 /name Michael Tan"));
    }

    @Test
    public void parseCommand_invalidSearchParticipant_propagatesParseException() {
        assertThrows(ParseException.class, () ->
                new AddressBookParser().parseCommand("search-participant 1 /name"));
    }
}
