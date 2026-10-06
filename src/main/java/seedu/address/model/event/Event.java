package seedu.address.model.event;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents an immutable event with a stable identity and a validated name.
 * Participant relationships, roles and attendance are managed separately.
 */
public final class Event {
    private final EventId id;
    private final EventName name;
    private final int capacity;
    private final String description;

    /**
     * Constructs an event with the supplied identity and name.
     * Reuses the identity when reconstructing or updating an existing event.
     */
    public Event(EventId id, EventName name, int capacity, String description) {
        requireAllNonNull(id, name, description);
        checkArgument(capacity > 0, "Event capacity must be a positive integer");
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    public String getId() {
        return id.toString();
    }

    public int getCapacity() {
        return capacity;
    }

    public String getDescription() {
        return description;
    }

    public EventName getName() {
        return name;
    }

    /**
     * Returns true if both events have the same ID, even if their details differ.
     */
    public boolean isSameEvent(Event otherEvent) {
        return otherEvent != null && id.equals(otherEvent.id);
    }

    /**
     * Returns true if both events have the same ID and details.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof Event otherEvent
                && id.equals(otherEvent.id)
                && name.equals(otherEvent.name)
                && capacity == otherEvent.capacity
                && description.equals(otherEvent.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, capacity, description);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("capacity", capacity)
                .add("description", description)
                .toString();
    }
}
