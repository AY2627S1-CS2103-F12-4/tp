package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class EventIdTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventId(null));
        assertFalse(EventId.isValidEventId(null));
    }

    @Test
    public void constructor_invalidId_throwsIllegalArgumentException() {
        for (String invalid : new String[] {"", "E", "P1", "e1", "1", "E-1", "E1.5", " E1", "E1 "}) {
            assertFalse(EventId.isValidEventId(invalid));
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class, () -> new EventId(invalid));
            assertEquals(EventId.MESSAGE_CONSTRAINTS, exception.getMessage());
        }
    }

    @Test
    public void constructor_validId_preservesValue() {
        for (String valid : new String[] {"E0", "E1", "E123", "E001", "E12345678901234567890"}) {
            assertTrue(EventId.isValidEventId(valid));
            EventId id = new EventId(valid);
            assertEquals(valid, id.value);
            assertEquals(valid, id.toString());
            assertEquals(id, new EventId(id.toString()));
        }
    }

    @Test
    public void equals_comparesId() {
        EventId id = new EventId("E1");
        EventId copy = new EventId("E1");
        assertEquals(id, id);
        assertEquals(id, copy);
        assertEquals(id.hashCode(), copy.hashCode());
        assertNotEquals(id, new EventId("E2"));
        assertNotEquals(id, new EventId("E01"));
        assertNotEquals(id, null);
        assertNotEquals(id, "E1");
    }
}
