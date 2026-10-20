package seedu.address.model.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.event.exceptions.EventNotFoundException;

public class EditEventListTest {
    @Test
    public void updateEvent_preservesOrderAndNextId() {
        EventList events = new EventList();
        Event first = events.createEvent("First", 10, "Old");
        Event second = events.createEvent("Second", 20, "Other");
        Event updated = new Event(new EventId(first.getId()), "First", 30, "New");
        events.updateEvent(updated);
        assertEquals(List.of(updated, second), events.getEvents());
        assertEquals(updated, events.getEventFromId(first.getId()));
        assertEquals("E3", events.createEvent("Next", 1, "").getId());
    }

    @Test
    public void updateEvent_nullOrMissing_rejectedWithoutChanges() {
        EventList events = new EventList();
        Event existing = events.createEvent("Party", 1, "");
        assertThrows(NullPointerException.class, () -> events.updateEvent(null));
        assertThrows(EventNotFoundException.class, () ->
                events.updateEvent(new Event(new EventId("E99"), "Missing", 1, "")));
        assertEquals(List.of(existing), events.getEvents());
        assertEquals("E2", events.createEvent("Next", 1, "").getId());
    }

    @Test
    public void modelUpdateEvent_invalid_preservesSelectionAndFilter() {
        ModelManager model = new ModelManager();
        Event existing = model.createEvent("Party", 1, "");
        model.selectEvent(new EventId(existing.getId()));
        model.updateFilteredEventList(unused -> false);
        assertThrows(NullPointerException.class, () -> model.updateEvent(null));
        assertThrows(EventNotFoundException.class, () ->
                model.updateEvent(new Event(new EventId("E99"), "Missing", 1, "")));
        assertEquals(List.of(existing), model.getEvents());
        assertEquals(existing, model.getSelectedEvent().orElseThrow());
        assertEquals(List.of(), model.getFilteredEventList());
    }
}
