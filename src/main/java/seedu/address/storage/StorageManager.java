package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.EventList;
import seedu.address.model.mapping.Participation;

/**
 * Manages application data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonAddressBookStorage addressBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    private JsonMappingStorage mappingStorage;

    /**
     * Creates a {@code StorageManager} with the given address book, user prefs and mapping storage.
     */
    public StorageManager(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage,
                          JsonMappingStorage mappingStorage) {
        this.addressBookStorage = addressBookStorage;
        this.userPrefsStorage = userPrefsStorage;
        this.mappingStorage = mappingStorage;
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

    // ================ Mapping methods ==============================

    @Override
    public Path getMappingFilePath() {
        return mappingStorage.getMappingFilePath();
    }

    @Override
    public Optional<List<Participation>> readMappings(EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + mappingStorage.getMappingFilePath());
        return mappingStorage.readMappings(eventList, addressBook);
    }

    @Override
    public void saveMappings(Collection<Participation> mappings) throws IOException {
        logger.fine("Attempting to write to data file: " + mappingStorage.getMappingFilePath());
        mappingStorage.saveMappings(mappings);
    }

}
