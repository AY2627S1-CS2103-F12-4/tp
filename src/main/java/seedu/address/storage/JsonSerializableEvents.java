package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;
import seedu.address.model.event.exceptions.DuplicateEventException;

/** JSON document containing the complete event collection. */
class JsonSerializableEvents {
    private final List<JsonAdaptedEvent> events = new ArrayList<>();

    /** Constructs a document from JSON, requiring an events array. */
    @JsonCreator
    public JsonSerializableEvents(@JsonProperty("events") List<JsonAdaptedEvent> events) {
        if (events == null) {
            throw new IllegalArgumentException("Events field must be present.");
        }
        this.events.addAll(events);
    }

    /** Takes an independent snapshot of the events to save. */
    public static JsonSerializableEvents fromEvents(List<Event> events) {
        return new JsonSerializableEvents(events.stream().map(JsonAdaptedEvent::new).toList());
    }

    /** Restores events and validates field constraints and unique IDs. */
    public EventList toModelType() throws IllegalValueException {
        EventList result = new EventList();
        for (JsonAdaptedEvent event : events) {
            if (event == null) {
                throw new IllegalValueException("Events list must not contain null entries.");
            }
            try {
                result.addEvent(event.toModelType());
            } catch (DuplicateEventException e) {
                throw new IllegalValueException("Events list contains duplicate event IDs.");
            }
        }
        return result;
    }
}
