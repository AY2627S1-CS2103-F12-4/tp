package seedu.address.storage;

import java.math.BigInteger;
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
    private final String nextEventId;

    /** Constructs a document from JSON, requiring an events array. */
    @JsonCreator
    public JsonSerializableEvents(@JsonProperty("events") List<JsonAdaptedEvent> events,
            @JsonProperty("nextEventId") String nextEventId) {
        if (events == null) {
            throw new IllegalArgumentException("Events field must be present.");
        }
        this.events.addAll(events);
        this.nextEventId = nextEventId;
    }

    /** Takes an independent snapshot of the events to save. */
    public static JsonSerializableEvents fromEvents(List<Event> events, BigInteger nextEventId) {
        return new JsonSerializableEvents(events.stream().map(JsonAdaptedEvent::new).toList(), nextEventId.toString());
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
        if (nextEventId != null) {
            if (!nextEventId.matches("[1-9][0-9]*")
                    || new BigInteger(nextEventId).compareTo(result.getNextId()) < 0) {
                throw new IllegalValueException("Next event ID must be positive and exceed all stored event IDs.");
            }
            result.restoreNextId(new BigInteger(nextEventId));
        }
        return result;
    }
}
