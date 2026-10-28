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
    public void parseAndExecute_unicodeWhitespaceName_preservesNameAndUpdatesOtherFields() throws Exception {
        for (String whitespace : new String[] {"\u2003", "\u2002", "\u3000", "\u2002 \u2003\u3000"}) {
            ModelManager model = new ModelManager();
            model.createEvent("Main", 10, "Original");
            parser.parseCommand("edit-event E1 " + whitespace + " /cap 2").execute(model);
            assertEquals(new Event(new EventId("E1"), "Main", 2, "Original"), model.getEvents().getFirst());
            parser.parseCommand("edit-event E1 " + whitespace + " /desc Updated").execute(model);
            assertEquals(new Event(new EventId("E1"), "Main", 2, "Updated"), model.getEvents().getFirst());
        }
    }

    @Test
    public void parse_unicodeWhitespaceNameWithoutOtherFields_reportsNoChanges() {
        for (String whitespace : new String[] {"\u2003", "\u2002", "\u3000"}) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parseCommand("edit-event E1 " + whitespace));
            assertEquals(EditEventCommand.MESSAGE_NO_CHANGES, exception.getMessage());
        }
    }

    @Test
    public void parse_unicodeWhitespaceAroundRealName_preservesInternalWhitespace() throws Exception {
        assertEquals(new EditEventCommand(new EventId("E1"), "New\u2003Name", 2, null),
                parser.parseCommand("edit-event E1 \u2002New\u2003Name\u3000 /cap 2"));
    }

    @Test
    public void parseAndExecute_validCommand_updatesEvent() throws Exception {
        ModelManager model = new ModelManager();
        model.addEvent(new Event(new EventId("E8"), "Welcome Party", 1, "Old"));
        parser.parseCommand("edit-event E8 Welcome Party /cap 100 /desc Meet new friends").execute(model);
        assertEquals(new Event(new EventId("E8"), "Welcome Party", 100, "Meet new friends"),
                model.getEvents().get(0));
    }

    @Test
    public void parse_reversedPrefixesAndWhitespace_acceptsCommand() throws Exception {
        assertEquals(new EditEventCommand(new EventId("E8"), "Welcome Party", Integer.MAX_VALUE, ""),
                parser.parseCommand("edit-event  E8 Welcome Party /desc /cap 2147483647"));
    }

    @Test
    public void parse_missingFields_throwsParseException() {
        for (String input : new String[] {"", " ", "/cap 10", "/desc Hello", "E1", "E1   "}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-event " + input));
        }
    }

    @Test
    public void parse_allNonemptyFieldCombinations_acceptsCommand() throws Exception {
        for (int mask = 1; mask < 8; mask++) {
            String name = (mask & 1) != 0 ? "New Name" : null;
            Integer capacity = (mask & 2) != 0 ? 20 : null;
            String description = (mask & 4) != 0 ? "" : null;
            String input = "edit-event E1" + (name == null ? "" : " " + name)
                    + (capacity == null ? "" : " /cap " + capacity)
                    + (description == null ? "" : " /desc");
            assertEquals(new EditEventCommand(new EventId("E1"), name, capacity, description),
                    parser.parseCommand(input));
        }
    }

    @Test
    public void parse_invalidEventId_rejected() {
        for (String id : new String[] {"1", "e1", "E", "E-1", "P1"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-event " + id + " /cap 10"));
        }
    }

    @Test
    public void parse_idOnly_reportsNoChanges() {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parseCommand("edit-event E1"));
        assertEquals(EditEventCommand.MESSAGE_NO_CHANGES, exception.getMessage());
    }

    @Test
    public void parse_whitespaceAndMultiwordName_preservesValues() throws Exception {
        assertEquals(new EditEventCommand(new EventId("E12"), "New Event Name", 1, "New description"),
                parser.parseCommand("edit-event\t E12\t New Event Name  /desc New description /cap 1"));
        assertEquals(new EditEventCommand(new EventId("E1"), null, 1, null),
                parser.parseCommand("edit-event E1 /cap 0001"));
    }

    @Test
    public void parse_invalidInput_doesNotChangeModel() {
        ModelManager model = new ModelManager();
        Event original = model.createEvent("Party", 10, "Old");
        for (String input : new String[] {"E1", "E1 /cap 0", "E1 /cap 1 /cap 2", "E1 /desc A /desc B"}) {
            assertThrows(ParseException.class, () -> parser.parseCommand("edit-event " + input).execute(model));
            assertEquals(java.util.List.of(original), model.getEvents());
        }
    }

    @Test
    public void parse_invalidCapacity_throwsParseException() {
        for (String capacity : new String[] {"", "0", "-1", "1.5", "abc", "2147483648", "+1"}) {
            ParseException exception = assertThrows(ParseException.class, () ->
                    parser.parseCommand("edit-event E1 Party /cap " + capacity + " /desc Hello"));
            assertEquals(EditEventCommand.MESSAGE_INVALID_CAPACITY, exception.getMessage());
        }
    }

    @Test
    public void parse_duplicatePrefixes_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parseCommand("edit-event E1 Party /cap 10 /cap 20 /desc Hello"));
        assertThrows(ParseException.class, () ->
                parser.parseCommand("edit-event E1 Party /cap 10 /desc Hello /desc World"));
    }

}
