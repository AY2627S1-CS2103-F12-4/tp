package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.Participation;
import seedu.address.model.mapping.ReadOnlyMappings;

/**
 * Accesses participation mappings stored as a JSON file on disk.
 */
public class JsonMappingStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonMappingStorage.class);

    private final Path filePath;

    /**
     * Creates mapping storage using the given file path.
     */
    public JsonMappingStorage(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    public Path getMappingFilePath() {
        return filePath;
    }

    /**
     * Returns mappings resolved against the current event list and address book.
     * Returns {@code Optional.empty()} if the storage file is not found.
     * Stored mappings with invalid or unknown IDs are omitted.
     *
     * @throws DataLoadingException if loading the mapping data failed.
     */
    public Optional<List<Participation>> readMappings(EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        return readMappings(filePath, eventList, addressBook);
    }

    /**
     * Similar to {@link #readMappings(EventList, AddressBook)}.
     */
    public Optional<List<Participation>> readMappings(Path filePath, EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        requireNonNull(filePath);
        requireNonNull(eventList);
        requireNonNull(addressBook);

        Optional<JsonSerializableMappings> jsonMappings = JsonUtil.readJsonFile(
                filePath, JsonSerializableMappings.class);
        if (jsonMappings.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonMappings.get().toModelType(eventList, addressBook));
        } catch (IllegalValueException e) {
            logger.info("Illegal values found in " + filePath + ": " + e.getMessage());
            throw new DataLoadingException(e);
        }
    }

    /**
     * Saves the given mappings to storage.
     *
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveMappings(ReadOnlyMappings mappings) throws IOException {
        saveMappings(mappings, filePath);
    }

    /**
     * Similar to {@link #saveMappings(ReadOnlyMappings)}.
     */
    public void saveMappings(ReadOnlyMappings mappings, Path filePath) throws IOException {
        requireNonNull(mappings);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableMappings(mappings), filePath);
    }
}
