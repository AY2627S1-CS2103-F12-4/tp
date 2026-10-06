package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventNameTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventName(null));
        assertThrows(NullPointerException.class, () -> EventName.isValidName(null));
    }

    @Test
    public void constructor_blank_throwsIllegalArgumentException() {
        for (String blank : new String[] {"", " ", "\t\n", "\u2003"}) {
            assertFalse(EventName.isValidName(blank));
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new EventName(blank));
            assertEquals(EventName.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void constructor_validNames_preservesValue() {
        for (String name : new String[] {"Workshop", "Open House 2026!", "Arts & Crafts", "活动", " Workshop "}) {
            assertTrue(EventName.isValidName(name));
            assertEquals(name, new EventName(name).toString());
        }
    }

    @Test
    public void equals_comparesName() {
        EventName name = new EventName("Workshop");
        EventName copy = new EventName("Workshop");
        assertEquals(name, name);
        assertEquals(name, copy);
        assertEquals(name.hashCode(), copy.hashCode());
        assertNotEquals(name, new EventName("Concert"));
        assertNotEquals(name, new EventName("workshop"));
        assertNotEquals(name, null);
        assertNotEquals(name, "Workshop");
    }
}
