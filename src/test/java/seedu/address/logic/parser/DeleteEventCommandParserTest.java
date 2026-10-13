package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.EventId;

public class DeleteEventCommandParserTest {
    @Test
    public void parse_validId_returnsCommand() throws Exception {
        DeleteEventCommand expected = new DeleteEventCommand(new EventId("E1"));
        assertEquals(expected, new DeleteEventCommandParser().parse(" E1 "));
        assertEquals(expected, new AddressBookParser().parseCommand(" del-event E1 "));
    }

    @Test
    public void parse_missingInvalidOrExtraArguments_throwsParseException() {
        for (String args : new String[] {"", " ", "1", "e1", "E-1", "E", "E1 E2", "E1 /cap 10"}) {
            assertThrows(ParseException.class, () -> new DeleteEventCommandParser().parse(args));
            assertThrows(ParseException.class, () -> new AddressBookParser().parseCommand("del-event " + args));
        }
    }
}
