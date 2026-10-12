package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddEventCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

public class AddEventCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void parseAndExecute_validCommand_createsEventAndResetsFilter() throws Exception {
        ModelManager model = new ModelManager();
        model.addEvent(new Event(new EventId("E8"), "Existing", 1, ""));
        model.updateFilteredEventList(unused -> false);
        String feedback = parser.parseCommand("add-event Welcome Party /cap 100 /desc Meet new friends")
                .execute(model).getFeedbackToUser();
        assertEquals("New event added: Welcome Party (E9)", feedback);
        assertEquals(2, model.getFilteredEventList().size());
        assertEquals(new Event(new EventId("E9"), "Welcome Party", 100, "Meet new friends"),
                model.getFilteredEventList().get(1));
    }

    @Test
    public void parse_missingFields_throwsParseException() {
        for (String input : new String[] {"", "Party", "Party /cap 10", "Party /desc Hello",
            "/cap 10 /desc Hello"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("add-event " + input));
        }
    }

    @Test
    public void parse_invalidCapacity_throwsParseException() {
        for (String capacity : new String[] {"", "0", "-1", "1.5", "abc", "2147483648", "+1"}) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parseCommand("add-event Party /cap " + capacity + " /desc Hello"));
            assertEquals(AddEventCommand.MESSAGE_INVALID_CAPACITY, exception.getMessage());
        }
    }

    @Test
    public void parse_duplicatePrefixes_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parseCommand("add-event Party /cap 10 /cap 20 /desc Hello"));
        assertThrows(ParseException.class, () ->
                parser.parseCommand("add-event Party /cap 10 /desc Hello /desc World"));
    }

    @Test
    public void execute_sameNames_assignsDistinctIds() throws Exception {
        ModelManager model = new ModelManager();
        parser.parseCommand("add-event Party /cap 10 /desc").execute(model);
        parser.parseCommand("add-event Party /cap 10 /desc").execute(model);
        assertEquals("E1", model.getFilteredEventList().get(0).getId());
        assertEquals("E2", model.getFilteredEventList().get(1).getId());
        assertEquals("", model.getFilteredEventList().get(0).getDescription());
    }
}
