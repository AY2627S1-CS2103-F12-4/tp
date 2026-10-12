package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;

/** Jackson-friendly representation of an event. */
class JsonAdaptedEvent {
    private final String id;
    private final String name;
    private final Integer capacity;
    private final String description;

    /** Constructs an adapted event from JSON fields. */
    @JsonCreator
    public JsonAdaptedEvent(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("capacity") Integer capacity, @JsonProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    /** Copies all persisted fields from the supplied event. */
    public JsonAdaptedEvent(Event source) {
        this(source.getId(), source.getName(), source.getCapacity(), source.getDescription());
    }

    /** Restores an event, reporting invalid or missing fields as a data loading error. */
    public Event toModelType() throws IllegalValueException {
        if (id == null || name == null || capacity == null || description == null) {
            throw new IllegalValueException("Event fields id, name, capacity and description must be present.");
        }
        try {
            return new Event(new EventId(id), name, capacity, description);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
