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
    private final String name;
    private final int capacity;
    private final String description;
    private final boolean archived;

    /**
     * Constructs an event with the supplied identity and name.
     * Reuses the identity when reconstructing or updating an existing event.
     */
    public Event(EventId id, String name, int capacity, String description) {
        this(id, name, capacity, description, false);
    }

    /** Constructs an event with its persisted archive state. */
    public Event(EventId id, String name, int capacity, String description, boolean archived) {
        requireAllNonNull(id, name, description);
        checkArgument(!name.isBlank(), "Event names should not be blank");
        checkArgument(capacity > 0, "Event capacity must be a positive integer");
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.description = description;
        this.archived = archived;
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

    public boolean isArchived() {
        return archived;
    }

    /** Returns a display label; the stored name remains unchanged for matching and searching. */
    public String getDisplayName() {
        return name + (archived ? " <archived>" : "");
    }

    /** Returns a copy preserving all details except archive state. */
    public Event withArchived(boolean archived) {
        return new Event(id, name, capacity, description, archived);
    }

    public String getName() {
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
                && archived == otherEvent.archived
                && capacity == otherEvent.capacity
                && description.equals(otherEvent.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, capacity, description, archived);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("id", id)
                .add("name", name)
                .add("capacity", capacity)
                .add("description", description)
                .add("archived", archived)
                .toString();
    }
}
