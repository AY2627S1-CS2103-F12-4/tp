package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.event.exceptions.DuplicateEventException;
import seedu.address.model.event.exceptions.EventNotFoundException;

public class EventListTest {
    private static final EventId ID = new EventId("E1");
    private static final EventId OTHER_ID = new EventId("E2");
    private static final Event EVENT = new Event(ID, "Workshop", 10, "Description");

    @Test
    public void createEvent_assignsSequentialIdsAndRegistersEvents() {
        EventList events = new EventList();
        Event first = events.createEvent("Workshop", 10, "First");
        Event second = events.createEvent("Workshop", 20, "Second");
        assertEquals("E1", first.getId());
        assertEquals("E2", second.getId());
        assertEquals("Workshop", first.getName());
        assertEquals(10, first.getCapacity());
        assertEquals("First", first.getDescription());
        assertSame(first, events.getEventFromId("E1"));
        assertSame(second, events.getEventFromId("E2"));
        assertEquals(List.of(first, second), events.getEvents());
    }

    @Test
    public void createEvent_existingIds_continuesAfterHighestNumber() {
        EventList events = new EventList();
        events.addEvent(new Event(new EventId("E010"), "Imported", 10, ""));
        events.addEvent(EVENT);
        assertEquals("E11", events.createEvent("New", 10, "").getId());
    }

    @Test
    public void createEvent_invalidDetails_doesNotConsumeIdOrAddEvent() {
        EventList events = new EventList();
        assertThrows(IllegalArgumentException.class, () -> events.createEvent(" ", 10, ""));
        assertThrows(IllegalArgumentException.class, () -> events.createEvent("Workshop", 0, ""));
        assertThrows(NullPointerException.class, () -> events.createEvent("Workshop", 10, null));
        assertEquals(List.of(), events.getEvents());
        assertEquals("E1", events.createEvent("Valid", 10, "").getId());
    }

    @Test
    public void createEvent_largeExistingId_doesNotOverflow() {
        EventList events = new EventList();
        events.addEvent(new Event(new EventId("E99999999999999999999"), "Imported", 10, ""));
        assertEquals("E100000000000000000000", events.createEvent("New", 10, "").getId());
    }

    @Test
    public void getEventFromId_matchesStableId() {
        EventList events = new EventList();
        assertThrows(EventNotFoundException.class, () -> events.getEventFromId(ID.toString()));
        events.addEvent(EVENT);
        assertSame(EVENT, events.getEventFromId(EVENT.getId()));
        assertThrows(EventNotFoundException.class, () -> events.getEventFromId(OTHER_ID.toString()));
        assertThrows(EventNotFoundException.class, () -> events.getEventFromId(""));
        assertThrows(EventNotFoundException.class, () -> events.getEventFromId("Workshop"));
        assertThrows(NullPointerException.class, () -> events.getEventFromId(null));
    }

    @Test
    public void addEvent_duplicateId_rejectsWithoutChangingExistingEvent() {
        EventList events = new EventList();
        events.addEvent(EVENT);
        Event renamed = new Event(ID, "Concert", 20, "Updated description");
        assertThrows(DuplicateEventException.class, () -> events.addEvent(EVENT));
        assertThrows(DuplicateEventException.class, () -> events.addEvent(renamed));
        assertEquals(List.of(EVENT), events.getEvents());
        assertSame(EVENT, events.getEventFromId(EVENT.getId()));
    }

    @Test
    public void addEvent_sameNameDifferentIds_keepsBothInInsertionOrder() {
        EventList events = new EventList();
        Event second = new Event(OTHER_ID, EVENT.getName(), 10, "Description");
        events.addEvent(EVENT);
        events.addEvent(second);
        assertEquals(List.of(EVENT, second), events.getEvents());
        assertSame(second, events.getEventFromId(second.getId()));
    }

    @Test
    public void addEvent_null_rejectsWithoutChangingList() {
        EventList events = new EventList();
        assertThrows(NullPointerException.class, () -> events.addEvent(null));
        assertEquals(List.of(), events.getEvents());
    }

    @Test
    public void getEvents_snapshotCannotModifyBackingCollection() {
        EventList events = new EventList();
        events.addEvent(EVENT);
        List<Event> snapshot = events.getEvents();
        assertThrows(UnsupportedOperationException.class, snapshot::clear);
        Event second = new Event(OTHER_ID, EVENT.getName(), 10, "Description");
        events.addEvent(second);
        assertEquals(List.of(EVENT), snapshot);
        assertEquals(List.of(EVENT, second), events.getEvents());
    }

    @Test
    public void removeEvent_existingId_removesOnlyTargetAndDoesNotReuseId() {
        EventList events = new EventList();
        Event first = events.createEvent("Workshop", 10, "");
        Event second = events.createEvent("Concert", 20, "");
        List<Event> snapshot = events.getEvents();
        events.removeEvent(second.getId());
        assertThrows(EventNotFoundException.class, () -> events.getEventFromId(second.getId()));
        assertSame(first, events.getEventFromId(first.getId()));
        assertEquals(List.of(first), events.getEvents());
        assertEquals(List.of(first, second), snapshot);
        assertEquals("E3", events.createEvent("New event", 10, "").getId());
    }

    @Test
    public void removeEvent_missingOrNullId_leavesCollectionAndNextIdUnchanged() {
        EventList events = new EventList();
        events.addEvent(EVENT);
        assertThrows(EventNotFoundException.class, () -> events.removeEvent("E2"));
        assertThrows(EventNotFoundException.class, () -> events.removeEvent(""));
        assertThrows(NullPointerException.class, () -> events.removeEvent(null));
        assertEquals(List.of(EVENT), events.getEvents());
        assertEquals("E2", events.createEvent("Next", 10, "").getId());
    }

    @Test
    public void removeEvent_lastEvent_keepsNextIdAndRejectsRepeatedRemoval() {
        EventList events = new EventList();
        events.addEvent(EVENT);
        events.removeEvent(EVENT.getId());
        assertEquals(List.of(), events.getEvents());
        assertThrows(EventNotFoundException.class, () -> events.removeEvent(EVENT.getId()));
        assertEquals("E2", events.createEvent("Next", 10, "").getId());
    }

    @Test
    public void independentLists_doNotShareEvents() {
        EventList first = new EventList();
        EventList second = new EventList();
        first.addEvent(EVENT);
        assertThrows(EventNotFoundException.class, () -> second.getEventFromId(EVENT.getId()));
    }
}
