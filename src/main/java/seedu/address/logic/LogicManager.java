package seedu.address.logic;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String FILE_OPS_ERROR_FORMAT = "Could not save data due to the following error: %s";

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            "Could not save data to file %s due to insufficient permissions to write to the file or the folder.";

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final Storage storage;
    private final AddressBookParser addressBookParser;
    private Set<Participation> savedParticipations;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this.model = model;
        this.storage = storage;
        addressBookParser = new AddressBookParser();
        savedParticipations = model.getParticipations().getParticipations();
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        CommandResult commandResult;
        Command command = addressBookParser.parseCommand(commandText);
        commandResult = command.execute(model);

        try {
            storage.saveAddressBook(model.getAddressBook());
            storage.saveEvents(model.getEvents());
            Set<Participation> participations = model.getParticipations().getParticipations();
            if (!savedParticipations.equals(participations)) {
                storage.saveParticipations(() -> participations);
                savedParticipations = participations;
            }
        } catch (AccessDeniedException e) {
            throw new CommandException(String.format(FILE_OPS_PERMISSION_ERROR_FORMAT, e.getMessage()), e);
        } catch (IOException ioe) {
            throw new CommandException(String.format(FILE_OPS_ERROR_FORMAT, ioe.getMessage()), ioe);
        }

        return commandResult;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public ObservableList<Event> getFilteredEventList() {
        return model.getFilteredEventList();
    }

    @Override
    public ObservableList<Participation> getParticipantSearchResults() {
        return model.getParticipantSearchResults();
    }

    @Override
    public List<Participation> getParticipantSearchResultsForEvent(Event event) {
        return model.getParticipantSearchResultsForEvent(event);
    }

    @Override
    public Optional<Event> getParticipantSearchEvent() {
        return model.getParticipantSearchEvent();
    }

    @Override
    public Optional<String> getParticipantSearchKeyword() {
        return model.getParticipantSearchKeyword();
    }

    @Override
    public boolean isParticipantSearchIncludingArchived() {
        return model.isParticipantSearchIncludingArchived();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }

    @Override
    public Set<Participation> getParticipationsForEvent(Event event) {
        return model.getParticipationsForEvent(event);
    }

    @Override
    public Optional<Event> getSelectedEvent() {
        return model.getSelectedEvent();
    }
}
