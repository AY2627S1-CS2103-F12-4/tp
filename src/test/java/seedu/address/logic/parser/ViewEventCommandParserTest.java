package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

public class ViewEventCommandParserTest {
    @Test
    public void parse_validId_returnsCommand() throws Exception {
        ViewEventCommand expected = new ViewEventCommand(new EventId("E1"));
        assertEquals(expected, new ViewEventCommandParser().parse(" E1 "));
        assertEquals(expected, new AddressBookParser().parseCommand(" view-event E1 "));
    }

    @Test
    public void parse_missingInvalidOrExtraArguments_throwsParseException() {
        for (String args : new String[] {"", " ", "1", "e1", "E-1", "E", "E1 P2", "E1 /role Host"}) {
            assertThrows(ParseException.class, () -> new ViewEventCommandParser().parse(args));
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("view-event " + args));
        }
    }
}
