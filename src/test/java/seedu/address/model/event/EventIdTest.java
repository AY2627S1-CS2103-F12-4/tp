package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class EventIdTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new EventId(null));
    }

    @Test
    public void constructor_generatedId_canBeReconstructed() {
        EventId id = new EventId();
        EventId restored = new EventId(UUID.fromString(id.toString()));
        assertEquals(id, restored);
        assertEquals(id.hashCode(), restored.hashCode());
    }

    @Test
    public void equals_comparesUuid() {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        EventId id = new EventId(uuid);
        assertEquals(id, id);
        assertEquals(id, new EventId(uuid));
        assertNotEquals(id, new EventId(UUID.fromString("00000000-0000-0000-0000-000000000002")));
        assertNotEquals(id, null);
        assertNotEquals(id, uuid);
    }
}
