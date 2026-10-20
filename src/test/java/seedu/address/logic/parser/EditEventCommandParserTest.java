package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

public class EditEventCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseAndExecute_validCommand_updatesEvent() throws Exception {
        ModelManager model = new ModelManager();
        model.addEvent(new Event(new EventId("E8"), "Welcome Party", 1, "Old"));
        parser.parseCommand("edit-event Welcome Party /cap 100 /desc Meet new friends").execute(model);
        assertEquals(new Event(new EventId("E8"), "Welcome Party", 100, "Meet new friends"),
                model.getEvents().get(0));
    }

    @Test
    public void parse_reversedPrefixesAndWhitespace_acceptsCommand() throws Exception {
        assertEquals(new EditEventCommand("Welcome Party", Integer.MAX_VALUE, ""),
                parser.parseCommand("edit-event  Welcome Party /desc /cap 2147483647"));
    }

    @Test
    public void parse_missingFields_throwsParseException() {
        for (String input : new String[] {"", "Party", "Party /cap 10", "Party /desc Hello",
            "/cap 10 /desc Hello"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-event " + input));
        }
    }

    @Test
    public void parse_invalidCapacity_throwsParseException() {
        for (String capacity : new String[] {"", "0", "-1", "1.5", "abc", "2147483648", "+1"}) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parseCommand("edit-event Party /cap " + capacity + " /desc Hello"));
            assertEquals(EditEventCommand.MESSAGE_INVALID_CAPACITY, exception.getMessage());
        }
    }

    @Test
    public void parse_duplicatePrefixes_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parseCommand("edit-event Party /cap 10 /cap 20 /desc Hello"));
        assertThrows(ParseException.class, () ->
                parser.parseCommand("edit-event Party /cap 10 /desc Hello /desc World"));
    }

}
