package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AddEventCommandTest {
    @Test
    public void equals_comparesAllEventDetails() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        AddEventCommand copy = new AddEventCommand("Orientation", 100, "Welcome session");

        assertTrue(command.equals(command));
        assertTrue(command.equals(copy));
        assertTrue(copy.equals(command));
        assertFalse(command.equals(null));
        assertFalse(command.equals("Orientation"));
        assertFalse(command.equals(new AddEventCommand("Workshop", 100, "Welcome session")));
        assertFalse(command.equals(new AddEventCommand("Orientation", 50, "Welcome session")));
        assertFalse(command.equals(new AddEventCommand("Orientation", 100, "Different description")));
    }

    @Test
    public void hashCode_equalCommands_returnsSameHashCode() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        AddEventCommand copy = new AddEventCommand("Orientation", 100, "Welcome session");

        assertEquals(command.hashCode(), copy.hashCode());
        assertEquals(command.hashCode(), command.hashCode());
    }

    @Test
    public void constructor_invalidDetails_throwsException() {
        assertThrows(NullPointerException.class, () -> new AddEventCommand(null, 100, "Welcome session"));
        assertThrows(NullPointerException.class, () -> new AddEventCommand("Orientation", 100, null));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand(" ", 100, "Welcome session"));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand("Orientation", 0, "Welcome session"));
        assertThrows(IllegalArgumentException.class, () -> new AddEventCommand("Orientation", -1, "Welcome session"));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        AddEventCommand command = new AddEventCommand("Orientation", 100, "Welcome session");
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }
}
