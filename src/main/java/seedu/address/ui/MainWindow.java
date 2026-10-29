package seedu.address.ui;

import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
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
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ArchiveEventCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteEventCommand;
import seedu.address.logic.commands.DeleteParticipantCommand;
import seedu.address.logic.commands.EditEventCommand;
import seedu.address.logic.commands.FindEventCommand;
import seedu.address.logic.commands.ListEventCommand;
import seedu.address.logic.commands.SearchParticipantCommand;
import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.logic.commands.UnarchiveEventCommand;
import seedu.address.logic.commands.ViewParticipantCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.event.Event;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.ui.dialogs.AddEventDialog;
import seedu.address.ui.dialogs.AddParticipantDialog;
import seedu.address.ui.dialogs.AddPersonDialog;
import seedu.address.ui.dialogs.DeleteConfirmationDialog;
import seedu.address.ui.dialogs.EditEventDialog;
import seedu.address.ui.dialogs.EditParticipantDialog;
import seedu.address.ui.dialogs.EditPersonDialog;
import seedu.address.ui.dialogs.EventEditConfirmationDialog;

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
    private ActionBar actionBar;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    private Event selectedEvent;
    private ParticipantDisplayMode participantDisplayMode = ParticipantDisplayMode.NONE;
    private boolean isExecutingCommand;
    private boolean isUpdatingParticipantPanel;

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
     * @param logic        application logic
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
     * @param menuItem       menu item receiving the accelerator
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
                logic.getParticipantSearchResults(),
                this::handleEditParticipant,
                this::handleDeleteParticipant);
        participantListPanelPlaceholder.getChildren()
                .add(participantListPanel.getRoot());

        eventListPanel = new EventListPanel(
                logic.getFilteredEventList(),
                this::handleEventSelection,
                this::handleAddParticipant,
                this::handleEditEvent,
                this::handleToggleEventArchive,
                this::handleDeleteEvent);
        eventListPanelPlaceholder.getChildren()
                .add(eventListPanel.getRoot());

        logic.getParticipantSearchResults().addListener(
                (ListChangeListener<Participation>) change -> {
                    if (!isExecutingCommand) {
                        updateParticipantPanel("");
                    }
                });

        actionBar = new ActionBar(
                this::handleAddPerson,
                this::handleAddEvent,
                this::handleClearSearch);
        actionBarPlaceholder.getChildren()
                .add(actionBar.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren()
                .add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren()
                .add(statusBarFooter.getRoot());

        CommandBox commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren()
                .add(commandBox.getRoot());
    }

    /**
     * Displays participants belonging to the selected event.
     *
     * @param event newly selected event, or {@code null} if cleared
     */
    private void handleEventSelection(Event event) {
        if (isExecutingCommand || isUpdatingParticipantPanel) {
            return;
        }

        if (participantDisplayMode == ParticipantDisplayMode.SEARCH
                && logic.getParticipantSearchKeyword().isPresent()) {
            if (event != null) {
                selectedEvent = event;
            }

            updateParticipantPanel("");
            return;
        }

        if (event == null) {
            selectedEvent = null;

            if (participantDisplayMode == ParticipantDisplayMode.EVENT) {
                participantDisplayMode = ParticipantDisplayMode.NONE;
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
        if (isUpdatingParticipantPanel) {
            return;
        }

        isUpdatingParticipantPanel = true;

        try {
            if (isParticipantPanelResetCommand(commandText)) {
                clearEventSelectionAndParticipantPanel();
                return;
            }

            if (isParticipantSearchCommand(commandText)) {
                startParticipantSearchDisplay();
                return;
            }

            if (participantDisplayMode == ParticipantDisplayMode.EVENT) {
                refreshSelectedEvent();
                return;
            }

            reconcileParticipantSearchDisplay();
        } finally {
            isUpdatingParticipantPanel = false;
        }
    }

    /**
     * Starts a new search display without retaining the previous event selection.
     */
    private void startParticipantSearchDisplay() {
        selectedEvent = null;
        if (logic.getParticipantSearchKeyword().isEmpty()) {
            clearEventSelectionAndParticipantPanel();
            return;
        }

        participantDisplayMode = ParticipantDisplayMode.SEARCH;
        refreshParticipantSearch();
    }

    /**
     * Refreshes an active search display or clears a display whose search has ended.
     */
    private void reconcileParticipantSearchDisplay() {
        if (logic.getParticipantSearchKeyword().isPresent()) {
            participantDisplayMode = ParticipantDisplayMode.SEARCH;
            refreshParticipantSearch();
            return;
        }

        if (participantDisplayMode == ParticipantDisplayMode.SEARCH) {
            clearEventSelectionAndParticipantPanel();
        }
    }

    /**
     * Clears event selection before resetting the participant panel.
     * Called while participant panel updates are guarded against reentrancy.
     */
    private void clearEventSelectionAndParticipantPanel() {
        eventListPanel.clearSelection();
        clearParticipantPanel();
    }

    /**
     * Selects and displays the event chosen by a participant command.
     */
    private void showCommandSelectedEvent() {
        Optional<Event> eventToDisplay = logic.getSelectedEvent()
                .flatMap(selected -> logic.getFilteredEventList().stream()
                        .filter(event -> event.isSameEvent(selected))
                        .findFirst());

        if (eventToDisplay.isEmpty()) {
            eventListPanel.clearSelection();
            clearParticipantPanel();
            return;
        }

        selectedEvent = eventToDisplay.orElseThrow();
        participantDisplayMode = ParticipantDisplayMode.EVENT;

        eventListPanel.selectEvent(selectedEvent);
        participantListPanel.showParticipantsForEvent(
                selectedEvent,
                logic.getParticipationsForEvent(selectedEvent));
    }

    /**
     * Refreshes participants belonging to the selected event.
     */
    private void refreshSelectedEvent() {
        if (selectedEvent == null) {
            clearParticipantPanel();
            return;
        }

        Optional<Event> currentEvent = logic.getFilteredEventList().stream()
                .filter(event -> event.getId()
                        .equals(selectedEvent.getId()))
                .findFirst();

        if (currentEvent.isEmpty()) {
            eventListPanel.clearSelection();
            clearParticipantPanel();
            return;
        }

        selectedEvent = currentEvent.orElseThrow();

        participantListPanel.showParticipantsForEvent(
                selectedEvent,
                logic.getParticipationsForEvent(selectedEvent));
    }

    /**
     * Refreshes the current participant search.
     */
    private void refreshParticipantSearch() {
        String keyword =
                logic.getParticipantSearchKeyword().orElseThrow();

        Optional<Event> currentEvent =
                logic.getFilteredEventList().stream()
                        .filter(event -> selectedEvent != null
                                && event.isSameEvent(selectedEvent))
                        .findFirst()
                        .or(() -> logic.getFilteredEventList().stream()
                                .findFirst());

        if (currentEvent.isPresent()) {
            selectedEvent = currentEvent.orElseThrow();
            eventListPanel.selectEvent(selectedEvent);

            participantListPanel.showSearchResultsForEvent(
                    selectedEvent,
                    keyword,
                    logic.getParticipantSearchResultsForEvent(
                            selectedEvent));
            return;
        }

        eventListPanel.clearSelection();
        selectedEvent = null;

        Optional<Event> scope =
                logic.getParticipantSearchEvent();

        if (scope.isPresent()) {
            participantListPanel.setSearchContext(
                    scope.orElseThrow(),
                    keyword);
        } else {
            participantListPanel.setSearchContext(keyword, logic.isParticipantSearchIncludingArchived());
        }
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
     * Returns whether the command clears participant selection and context.
     */
    private boolean isParticipantPanelResetCommand(String commandText) {
        String commandWord = commandText.trim().split("\\s+", 2)[0];
        return commandWord.equals(FindEventCommand.COMMAND_WORD)
                || commandWord.equals(ListEventCommand.COMMAND_WORD);
    }

    /**
     * Returns whether the command displays participant search results.
     */
    private boolean isParticipantSearchCommand(String commandText) {
        String trimmedCommand = commandText.trim();

        if (trimmedCommand.isEmpty()) {
            return false;
        }

        String commandWord = trimmedCommand.split("\\s+", 2)[0];

        return commandWord.equals(SearchParticipantCommand.COMMAND_WORD)
                || commandWord.equals(SearchRoleCommand.COMMAND_WORD);
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

    private void handleClearSearch() {
        try {
            executeCommand(ListEventCommand.COMMAND_WORD);
        } catch (CommandException | ParseException exception) {
            // executeCommand already displays the error.
        }
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
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(
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
        AddParticipantDialog dialog = new AddParticipantDialog(
                primaryStage,
                event.getId(),
                event.getName());

        dialog.showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(eventsTab);
        });
    }

    private void handleEditParticipant(Participation participation) {
        new EditParticipantDialog(primaryStage, participation).showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(eventsTab);
        });
    }

    private void handleDeleteParticipant(Participation participation) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(
                primaryStage,
                "Delete participant",
                "Remove " + participation.getPerson().getName().fullName
                        + " from " + participation.getEvent().getName() + "?");

        if (dialog.showAndWait()) {
            executeDeleteCommand(DeleteParticipantCommand.COMMAND_WORD + " "
                    + participation.getEvent().getId() + " "
                    + participation.getPerson().getId(), eventsTab);
        }
    }

    private void handleEditEvent(Event event) {
        new EditEventDialog(primaryStage, event).showAndWait(command -> {
            executeCommand(command);
            listTabs.getSelectionModel().select(eventsTab);
        });
    }

    private void handleToggleEventArchive(Event event) {
        try {
            executeCommand(buildArchiveCommand(event));
            listTabs.getSelectionModel().select(eventsTab);
        } catch (CommandException | ParseException exception) {
            // executeCommand already displays the error.
        }
    }

    /** Returns the command that toggles the supplied event's archive state. */
    static String buildArchiveCommand(Event event) {
        String commandWord = event.isArchived()
                ? UnarchiveEventCommand.COMMAND_WORD
                : ArchiveEventCommand.COMMAND_WORD;
        return commandWord + " " + event.getId();
    }

    private void handleDeleteEvent(Event event) {
        DeleteConfirmationDialog dialog = new DeleteConfirmationDialog(
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
        WindowBounds.fit(primaryStage);
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
        // Reconcile selection after the command finishes publishing its event filter
        // and result rows.
        isExecutingCommand = true;

        try {
            if (commandText.stripLeading().split("\\s+", 2)[0].equals(EditEventCommand.COMMAND_WORD)) {
                EditEventCommand edit = (EditEventCommand) new AddressBookParser().parseCommand(commandText);
                Optional<String> preview = edit.getConfirmationMessage();
                if (preview.isPresent()
                        && !new EventEditConfirmationDialog(primaryStage, preview.orElseThrow()).showAndWait()) {
                    throw new CommandException("Event edit cancelled. Your input has been kept.");
                }
            }
            CommandResult commandResult = logic.execute(commandText);

            logger.info(
                    "Result: "
                            + commandResult.getFeedbackToUser());

            resultDisplay.setFeedbackToUser(
                    isParticipantSearchCommand(commandText)
                            || commandText.stripLeading().split("\\s+", 2)[0]
                            .equals(EditEventCommand.COMMAND_WORD)
                            || FindEventCommand.MESSAGE_NO_MATCHES.equals(
                            commandResult.getFeedbackToUser())
                            || commandText.stripLeading().split("\\s+", 2)[0]
                            .equals(ViewParticipantCommand.COMMAND_WORD)
                            || commandText.stripLeading().split("\\s+", 2)[0]
                            .equals(AddCommand.COMMAND_WORD)
                            ? commandResult.getFeedbackToUser()
                            : CommandSuccessMessages.forCommand(commandText));

            if (commandResult.shouldShowParticipants()) {
                showCommandSelectedEvent();
            } else {
                updateParticipantPanel(commandText);
            }

            actionBar.setClearSearchVisible(
                    logic.getParticipantSearchKeyword().isPresent());

            if (commandResult.shouldShowPeople()) {
                listTabs.getSelectionModel().select(peopleTab);
            }

            if (commandResult.shouldShowParticipants()
                    || commandResult.shouldShowEvents()) {
                listTabs.getSelectionModel().select(eventsTab);
            }

            if (commandResult.shouldShowHelp()) {
                handleHelp();
            }

            if (commandResult.shouldExit()) {
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
        } finally {
            isExecutingCommand = false;
        }
    }
}
