package seedu.address.model.event;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores events by stable ID in insertion order. Different events may share a name.
 */
public class EventList {
    private final Map<String, Event> events = new LinkedHashMap<>();

    /**
     * Adds an event without overwriting another event with the same ID.
     *
     * @throws NullPointerException if the event is null.
     * @throws IllegalArgumentException if the ID is already registered.
     */
    public void addEvent(Event event) {
        requireNonNull(event);
        checkArgument(!events.containsKey(event.getId()), "An event with this ID already exists");
        events.put(event.getId(), event);
    }

    /**
     * Returns the event with the supplied ID, or null if no event matches.
     *
     * @throws NullPointerException if the ID is null.
     */
    public Event getEventFromId(String id) {
        requireNonNull(id);
        return events.get(id);
    }

    /**
     * Returns an immutable snapshot of the events in insertion order.
     */
    public List<Event> getEvents() {
        return List.copyOf(events.values());
    }
}
