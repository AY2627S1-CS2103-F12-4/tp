package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;

/** Reads and writes events in their own JSON file. */
public class JsonEventStorage {
    private final Path filePath;

    public JsonEventStorage(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    public Path getEventFilePath() {
        return filePath;
    }

    /** Returns empty for a missing file and reports malformed data as a loading error. */
    public Optional<EventList> readEvents() throws DataLoadingException {
        Optional<JsonSerializableEvents> document = JsonUtil.readJsonFile(filePath, JsonSerializableEvents.class);
        if (document.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(document.get().toModelType());
        } catch (IllegalValueException e) {
            throw new DataLoadingException(e);
        }
    }

    /** Saves the complete collection, overwriting the previous event document. */
    public void saveEvents(List<Event> events) throws IOException {
        EventList snapshot = new EventList();
        events.forEach(snapshot::addEvent);
        saveEvents(events, snapshot.getNextId());
    }

    /** Saves the events and the sequence needed to avoid reusing deleted IDs after restart. */
    public void saveEvents(List<Event> events, BigInteger nextEventId) throws IOException {
        requireNonNull(events);
        JsonSerializableEvents document = JsonSerializableEvents.fromEvents(events, nextEventId);
        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(document, filePath);
    }
}
