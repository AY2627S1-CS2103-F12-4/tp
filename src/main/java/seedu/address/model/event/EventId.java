package seedu.address.model.event;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

/**
 * Represents an immutable event identifier, independent of its details or list position.
 */
public final class EventId {
    private final UUID value;

    /**
     * Creates a new event identifier.
     */
    public EventId() {
        this(UUID.randomUUID());
    }

    /**
     * Constructs an identifier from an existing UUID, allowing identity to be preserved on reload.
     */
    public EventId(UUID value) {
        this.value = requireNonNull(value);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EventId otherId && value.equals(otherId.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
