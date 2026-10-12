package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.EventList;
import seedu.address.model.mapper.ReadOnlyParticipations;

/**
 * Manages application data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private JsonAddressBookStorage addressBookStorage;
    private JsonUserPrefsStorage userPrefsStorage;
    private JsonParticipationStorage participationStorage;

    /**
     * Creates a {@code StorageManager} with the given address book, user prefs and
     * participation storage.
     */
    public StorageManager(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage,
            JsonParticipationStorage participationStorage) {
        this.addressBookStorage = addressBookStorage;
        this.userPrefsStorage = userPrefsStorage;
        this.participationStorage = participationStorage;
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

    // ================ Participation methods ==============================

    @Override
    public Path getParticipationFilePath() {
        return participationStorage.getParticipationFilePath();
    }

    @Override
    public Optional<ReadOnlyParticipations> readParticipations(EventList eventList, AddressBook addressBook)
            throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + participationStorage.getParticipationFilePath());
        return participationStorage.readParticipations(eventList, addressBook);
    }

    @Override
    public void saveParticipations(ReadOnlyParticipations participations) throws IOException {
        logger.fine("Attempting to write to data file: " + participationStorage.getParticipationFilePath());
        participationStorage.saveParticipations(participations);
    }

}
