package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventList;

/**
 * Manages storage of AddressBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonAddressBookStorage addressBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    private final JsonEventStorage eventStorage;

    /**
     * Creates a {@code StorageManager} with the given address book and user prefs storage.
     */
    public StorageManager(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this(addressBookStorage, userPrefsStorage,
                new JsonEventStorage(addressBookStorage.getAddressBookFilePath().resolveSibling("events.json")));
    }

    /** Creates storage with an explicitly configured independent event file. */
    public StorageManager(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage,
            JsonEventStorage eventStorage) {
        this.addressBookStorage = addressBookStorage;
        this.userPrefsStorage = userPrefsStorage;
        this.eventStorage = eventStorage;
    }

    @Override
    public Path getEventFilePath() {
        return eventStorage.getEventFilePath();
    }

    @Override
    public Optional<EventList> readEvents() throws DataLoadingException {
        return eventStorage.readEvents();
    }

    @Override
    public void saveEvents(List<Event> events) throws IOException {
        eventStorage.saveEvents(events);
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ AddressBook methods ==============================

    @Override
    public Path getAddressBookFilePath() {
        return addressBookStorage.getAddressBookFilePath();
    }

    @Override
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + addressBookStorage.getAddressBookFilePath());
        return addressBookStorage.readAddressBook();
    }

    @Override
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        logger.fine("Attempting to write to data file: " + addressBookStorage.getAddressBookFilePath());
        addressBookStorage.saveAddressBook(addressBook);
    }

}
