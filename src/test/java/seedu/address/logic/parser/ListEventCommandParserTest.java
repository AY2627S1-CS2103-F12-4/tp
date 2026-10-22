package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ListEventCommandParserTest {
    @Test
    public void parse_emptyOrWhitespace_returnsListEventCommand() throws Exception {
        ListEventCommandParser parser = new ListEventCommandParser();
        assertEquals(new ListEventCommand(), parser.parse(""));
        assertEquals(new ListEventCommand(), parser.parse(" \t "));
        assertEquals(new ListEventCommand(), parser.parse("\u2003\u00a0"));
        assertEquals(new ListEventCommand(), new AddressBookParser().parseCommand("  list-event  "));
    }

    @Test
    public void parse_archiveFlag_returnsArchiveListingCommand() throws Exception {
        ListEventCommandParser parser = new ListEventCommandParser();
        assertEquals(new ListEventCommand(true), parser.parse("/include-archive"));
        assertEquals(new ListEventCommand(true), parser.parse("\u2003\t/include-archive\u00a0"));
        assertEquals(new ListEventCommand(true),
                new AddressBookParser().parseCommand("list-event /include-archive"));
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListEventCommandParser().parse(null));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListEventCommand.MESSAGE_USAGE);
        for (String args : new String[] {"E1", "1", "/cap 10", "/include-archive /include-archive",
            "/include-archive true", "/include-archive extra", "extra /include-archive", "/Include-Archive",
            "/include-archivex", "/include-archive=true", "/event E1 /include-archive"}) {
            assertThrows(ParseException.class, message, () -> new ListEventCommandParser().parse(args));
            assertThrows(ParseException.class, message, () ->
                    new AddressBookParser().parseCommand("list-event " + args));
        }
    }
}
