package seedu.address.model.event;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import seedu.address.model.event.exceptions.DuplicateEventException;
import seedu.address.model.event.exceptions.EventNotFoundException;

/**
 * Stores events by stable ID in insertion order. Different events may share a name.
 */
public class EventList {
    private final Map<String, Event> events = new LinkedHashMap<>();
    private BigInteger nextId = BigInteger.ONE;

    /**
     * Creates and adds an event with an automatically assigned ID.
     * Failed validation leaves the collection and next ID unchanged.
     */
    public Event createEvent(String name, int capacity, String description) {
        Event event = new Event(new EventId("E" + nextId), name, capacity, description);
        addEvent(event);
        return event;
    }

    /**
     * Adds an event without overwriting another event with the same ID.
     *
     * @throws NullPointerException if the event is null.
     * @throws DuplicateEventException if the ID is already registered.
     */
    public void addEvent(Event event) {
        requireNonNull(event);
        if (events.containsKey(event.getId())) {
            throw new DuplicateEventException();
        }
        events.put(event.getId(), event);
        BigInteger followingId = new BigInteger(event.getId().substring(1)).add(BigInteger.ONE);
        nextId = nextId.max(followingId);
    }

    /**
     * Returns the event with the supplied ID.
     *
     * @throws NullPointerException if the ID is null.
     * @throws EventNotFoundException if no event matches the ID.
     */
    public Event getEventFromId(String id) {
        requireNonNull(id);
        Event event = events.get(id);
        if (event == null) {
            throw new EventNotFoundException();
        }
        return event;
    }

    /**
     * Removes the event with the supplied ID, preserving the IDs of other events.
     * Deleted IDs are not reused by subsequent creation in this list.
     *
     * @throws NullPointerException if the ID is null.
     * @throws EventNotFoundException if no event matches the ID.
     */
    public void removeEvent(String id) {
        requireNonNull(id);
        if (events.remove(id) == null) {
            throw new EventNotFoundException();
        }
    }

    /**
     * Returns an immutable snapshot of the events in insertion order.
     */
    public List<Event> getEvents() {
        return List.copyOf(events.values());
    }
}
