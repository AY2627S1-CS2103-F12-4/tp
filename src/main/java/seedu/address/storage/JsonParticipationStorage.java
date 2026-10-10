package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.FileUtil;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.ReadOnlyParticipations;

/**
 * Accesses participations stored as a JSON file on disk.
 */
public class JsonParticipationStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonParticipationStorage.class);

    private final Path filePath;

    /**
     * Creates participation storage using the given file path.
     */
    public JsonParticipationStorage(Path filePath) {
        this.filePath = requireNonNull(filePath);
    }

    public Path getParticipationFilePath() {
        return filePath;
    }

    /**
     * Returns participations resolved against the current event list and address book.
     * Returns {@code Optional.empty()} if the storage file is not found.
     * Stored participations with invalid or unknown IDs are omitted.
     *
     * @throws DataLoadingException if loading the participation data failed.
     */
    public Optional<ReadOnlyParticipations> readParticipations(EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        return readParticipations(filePath, eventList, addressBook);
    }

    /**
     * Similar to {@link #readParticipations(EventList, AddressBook)}.
     */
    public Optional<ReadOnlyParticipations> readParticipations(
            Path filePath, EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        requireNonNull(filePath);
        requireNonNull(eventList);
        requireNonNull(addressBook);

        Optional<JsonSerializableParticipations> jsonParticipations = JsonUtil.readJsonFile(
                filePath, JsonSerializableParticipations.class);
        if (jsonParticipations.isEmpty()) {
            return Optional.empty();
        }

        try {
            return Optional.of(jsonParticipations.get().toModelType(eventList, addressBook));
        } catch (IllegalValueException e) {
            logger.info("Illegal values found in " + filePath + ": " + e.getMessage());
            throw new DataLoadingException(e);
        }
    }

    /**
     * Saves the given participations to storage.
     *
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveParticipations(ReadOnlyParticipations participations) throws IOException {
        saveParticipations(participations, filePath);
    }

    /**
     * Similar to {@link #saveParticipations(ReadOnlyParticipations)}.
     */
    public void saveParticipations(ReadOnlyParticipations participations, Path filePath) throws IOException {
        requireNonNull(participations);
        requireNonNull(filePath);

        FileUtil.createIfMissing(filePath);
        JsonUtil.saveJsonFile(new JsonSerializableParticipations(participations), filePath);
    }
}
