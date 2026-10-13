package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class ListEventCommandParserTest {
    @Test
    public void parse_emptyOrWhitespace_returnsListEventCommand() throws Exception {
        ListEventCommandParser parser = new ListEventCommandParser();
        assertInstanceOf(ListEventCommand.class, parser.parse(""));
        assertInstanceOf(ListEventCommand.class, parser.parse(" \t "));
        assertInstanceOf(ListEventCommand.class, new AddressBookParser().parseCommand("  list-event  "));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListEventCommand.MESSAGE_USAGE);
        for (String args : new String[] {"E1", "1", "/cap 10"}) {
            assertThrows(ParseException.class, message, () -> new ListEventCommandParser().parse(args));
            assertThrows(ParseException.class, message, () ->
                    new AddressBookParser().parseCommand("list-event " + args));
        }
    }
}
