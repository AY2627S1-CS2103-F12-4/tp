package seedu.address.ui;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Event;
import seedu.address.model.person.Person;
import seedu.address.ui.dialogs.AddEventDialog;
import seedu.address.ui.dialogs.AddParticipantDialog;
import seedu.address.ui.dialogs.AddPersonDialog;
import seedu.address.ui.dialogs.DeleteConfirmationDialog;
import seedu.address.ui.dialogs.EditPersonDialog;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";

    private enum ParticipantDisplayMode {
        NONE,
        EVENT,
        SEARCH
    }

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    private PersonListPanel personListPanel;
    private EventListPanel eventListPanel;
    private ParticipantListPanel participantListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    private Event selectedEvent;
    private ParticipantDisplayMode participantDisplayMode =
            ParticipantDisplayMode.NONE;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane actionBarPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane eventListPanelPlaceholder;

    @FXML
    private TabPane listTabs;

    @FXML
    private Tab peopleTab;

    @FXML
    private Tab eventsTab;

    @FXML
    private StackPane participantListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given dependencies.
     *
     * @param primaryStage primary application stage
     * @param logic application logic
     * @param dataFilePath path shown in the status bar
     */
    public MainWindow(
            Stage primaryStage,
            Logic logic,
            Path dataFilePath) {
        super(FXML, primaryStage);

        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        setWindowDefaultSize(logic.getGuiSettings());
        setAccelerators();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(
                helpMenuItem,
                KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     *
     * @param menuItem menu item receiving the accelerator
     * @param keyCombination accelerator key combination
     */
    private void setAccelerator(
            MenuItem menuItem,
            KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: This event filter can be removed after JDK-8131666
         * is fixed in the JavaFX SDK used by the application.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl
                    && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Fills all placeholders in this window.
     */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(
                logic.getFilteredPersonList(),
                this::handleEditPerson,
                this::handleDeletePerson);
        personListPanelPlaceholder.getChildren()
                .add(personListPanel.getRoot());

        participantListPanel = new ParticipantListPanel(
                logic.getParticipantSearchResults());
        participantListPanelPlaceholder.getChildren()
                .add(participantListPanel.getRoot());

        eventListPanel = new EventListPanel(
                logic.getFilteredEventList(),
                this::handleEventSelection,
                this::handleAddParticipant,
                this::handleDeleteEvent);
        eventListPanelPlaceholder.getChildren()
                .add(eventListPanel.getRoot());

        ActionBar actionBar = new ActionBar(
                this::handleAddPerson,
                this::handleAddEvent);
        actionBarPlaceholder.getChildren()
                .add(actionBar.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren()
                .add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter =
                new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren()
                .add(statusBarFooter.getRoot());

        CommandBox commandBox =
                new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren()
                .add(commandBox.getRoot());
    }

    /**
     * Displays participants belonging to the selected event.
     *
     * @param event newly selected event, or {@code null} if cleared
     */
    private void handleEventSelection(Event event) {
        if (event == null) {
            selectedEvent = null;

            if (participantDisplayMode
                    == ParticipantDisplayMode.EVENT) {
                participantDisplayMode =
                        ParticipantDisplayMode.NONE;
                participantListPanel.clearSearchContext();
            }

            return;
        }

        selectedEvent = event;
        participantDisplayMode = ParticipantDisplayMode.EVENT;

        participantListPanel.showParticipantsForEvent(
                event,
                logic.getParticipationsForEvent(event));
    }

    /**
     * Updates the participant panel after a successful command.
     *
     * @param commandText command that was executed
     */
    private void updateParticipantPanel(String commandText) {
        if (isParticipantSearchCommand(commandText)) {
            eventListPanel.clearSelection();
            selectedEvent = null;
            participantDisplayMode =
                    ParticipantDisplayMode.SEARCH;
        }

        if (participantDisplayMode
                == ParticipantDisplayMode.EVENT) {
            refreshSelectedEvent();
        } else if (participantDisplayMode
                == ParticipantDisplayMode.SEARCH) {
            refreshParticipantSearch();
        }
    }

    /**
     * Refreshes participants belonging to the selected event.
     */
    private void refreshSelectedEvent() {
        if (selectedEvent == null) {
            clearParticipantPanel();
            return;
        }

        Optional<Event> currentEvent =
                logic.getFilteredEventList().stream()
                        .filter(event -> event.getId()
                                .equals(selectedEvent.getId()))
                        .findFirst();

        if (currentEvent.isEmpty()) {
            eventListPanel.clearSelection();
            clearParticipantPanel();
            return;
        }

        selectedEvent = currentEvent.get();

        participantListPanel.showParticipantsForEvent(
                selectedEvent,
                logic.getParticipationsForEvent(selectedEvent));
    }

    /**
     * Refreshes the current participant search.
     */
    private void refreshParticipantSearch() {
        logic.getParticipantSearchEvent().ifPresentOrElse(
                event -> participantListPanel.setSearchContext(
                        event,
                        logic.getParticipantSearchKeyword()
                                .orElseThrow()),
                this::clearParticipantPanel);
    }

    /**
     * Clears the selected event and participant panel.
     */
    private void clearParticipantPanel() {
        selectedEvent = null;
        participantDisplayMode = ParticipantDisplayMode.NONE;
        participantListPanel.clearSearchContext();
    }

    /**
     * Returns whether the command displays participant search results.
     */
    private boolean isParticipantSearchCommand(String commandText) {
        String trimmedCommand = commandText.trim();

        if (trimmedCommand.isEmpty()) {
            return false;
        }

        String commandWord =
                trimmedCommand.split("\\s+", 2)[0];

        return commandWord.equals("search-participant")
                || commandWord.equals("search-role");
    }

    /**
     * Sets the default size based on the supplied GUI settings.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());

        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(
                    guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(
                    guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses it if it is already open.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    private void handleAddPerson() {
        new AddPersonDialog(primaryStage).showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(peopleTab);
        });
    }

    private void handleAddEvent() {
        new AddEventDialog(primaryStage).showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(eventsTab);
        });
    }

    private void handleEditPerson(Person person, int displayedIndex) {
        new EditPersonDialog(primaryStage, person, displayedIndex).showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(peopleTab);
        });
    }

    private void handleDeletePerson(
            Person person,
            int displayedIndex) {
        DeleteConfirmationDialog dialog =
                new DeleteConfirmationDialog(
                        primaryStage,
                        "Delete person",
                        "Delete "
                                + person.getName().fullName
                                + "?");

        if (dialog.showAndWait()) {
            executeDeleteCommand(
                    DeleteCommand.COMMAND_WORD
                            + " "
                            + displayedIndex,
                    peopleTab);
        }
    }

    private void handleAddParticipant(Event event) {
        AddParticipantDialog dialog =
                new AddParticipantDialog(
                        primaryStage,
                        event.getId(),
                        event.getName());

        dialog.showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(eventsTab);
        });
    }

    private void handleDeleteEvent(Event event) {
        DeleteConfirmationDialog dialog =
                new DeleteConfirmationDialog(
                        primaryStage,
                        "Delete event",
                        "Delete "
                                + event.getName()
                                + " ("
                                + event.getId()
                                + ")?");

        if (dialog.showAndWait()) {
            executeDeleteCommand(
                    DeleteEventCommand.COMMAND_WORD
                            + " "
                            + event.getId(),
                    eventsTab);
        }
    }

    private void executeDeleteCommand(
            String command,
            Tab targetTab) {
        try {
            executeCommand(command);
            listTabs.getSelectionModel().select(targetTab);
        } catch (CommandException | ParseException exception) {
            // executeCommand already displays the error.
        }
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(
                primaryStage.getWidth(),
                primaryStage.getHeight(),
                (int) primaryStage.getX(),
                (int) primaryStage.getY());

        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    ParticipantListPanel getParticipantListPanel() {
        return participantListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText)
            throws CommandException, ParseException {
        try {
            CommandResult commandResult =
                    logic.execute(commandText);

            logger.info(
                    "Result: "
                            + commandResult.getFeedbackToUser());

            resultDisplay.setFeedbackToUser(
                    CommandSuccessMessages.forCommand(commandText));

            updateParticipantPanel(commandText);

            // Participant-related commands now use the Events tab.
            if (commandResult.isShowParticipants()
                    || commandResult.isShowEvents()) {
                listTabs.getSelectionModel().select(eventsTab);
            }

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException exception) {
            logger.info(
                    "An error occurred while executing command: "
                            + commandText);

            resultDisplay.setFeedbackToUser(
                    exception.getMessage());
            throw exception;
        }
    }
}
