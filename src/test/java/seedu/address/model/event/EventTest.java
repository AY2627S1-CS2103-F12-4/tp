package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventTest {
    private static final EventId ID = new EventId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final EventId OTHER_ID = new EventId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final EventName NAME = new EventName("Workshop");

    @Test
    public void constructor_nullFields_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Event(null, NAME, 10, "Description"));
        assertThrows(NullPointerException.class, () -> new Event(ID, null, 10, "Description"));
    }

    @Test
    public void isSameEvent_usesIdRatherThanName() {
        Event event = new Event(ID, NAME, 10, "Description");
        Event renamed = new Event(ID, new EventName("Renamed workshop"), 10, "Description");
        Event sameName = new Event(OTHER_ID, NAME, 10, "Description");
        assertTrue(event.isSameEvent(event));
        assertTrue(event.isSameEvent(renamed));
        assertTrue(renamed.isSameEvent(event));
        assertFalse(event.isSameEvent(sameName));
        assertFalse(event.isSameEvent(null));
    }

    @Test
    public void equals_comparesIdentityAndDetails() {
        Event event = new Event(ID, NAME, 10, "Description");
        Event copy = new Event(new EventId(UUID.fromString(ID.toString())), NAME, 10, "Description");
        assertEquals(event, event);
        assertEquals(event, copy);
        assertEquals(copy, event);
        assertEquals(event.hashCode(), copy.hashCode());
        assertNotEquals(event, new Event(ID, new EventName("Renamed workshop"), 10, "Description"));
        assertNotEquals(event, new Event(OTHER_ID, NAME, 10, "Description"));
        assertNotEquals(event, new Event(ID, NAME, 20, "Description"));
        assertNotEquals(event, new Event(ID, NAME, 10, "Other description"));
        assertNotEquals(event, null);
        assertNotEquals(event, "Workshop");
    }

    @Test
    public void renamedEvent_preservesMappingKey() {
        Event original = new Event(ID, NAME, 10, "Description");
        Map<String, String> registrations = new HashMap<>();
        registrations.put(original.getId(), "Participant registration");
        Event renamed = new Event(ID, new EventName("New name"), 10, "Description");
        assertEquals("Participant registration", registrations.get(renamed.getId()));
        assertEquals(new EventName("New name"), renamed.getName());
        assertEquals(NAME, original.getName());
    }

    @Test
    public void constructor_invalidCapacity_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Event(ID, NAME, 0, "Description"));
        assertThrows(IllegalArgumentException.class, () -> new Event(ID, NAME, -1, "Description"));
    }

    @Test
    public void constructor_descriptionAndCapacity_preservesDetails() {
        assertThrows(NullPointerException.class, () -> new Event(ID, NAME, 1, null));
        Event event = new Event(ID, NAME, 1, "");
        assertEquals(1, event.getCapacity());
        assertEquals("", event.getDescription());
        Event largeEvent = new Event(ID, NAME, Integer.MAX_VALUE, "Large event");
        assertEquals(Integer.MAX_VALUE, largeEvent.getCapacity());
        assertEquals("Large event", largeEvent.getDescription());
        assertTrue(event.isSameEvent(largeEvent));
    }

    @Test
    public void toString_containsIdentityAndName() {
        Event event = new Event(ID, NAME, 10, "Description");
        assertEquals(Event.class.getCanonicalName() + "{id=" + ID + ", name=" + NAME
                + ", capacity=10, description=Description}", event.toString());
    }
}
