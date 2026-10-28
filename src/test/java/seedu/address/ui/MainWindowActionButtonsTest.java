package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.Messages;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainWindowActionButtonsTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void viewEvent_afterListingAnotherEvent_switchesSelectionAndParticipants() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Event first = model.createEvent("Workshop", 10, "First");
            Event second = model.createEvent("Workshop", 10, "Second");
            model.addPerson(ALICE);
            model.addParticipation(first, ALICE);
            MainWindow window = createWindow(model);
            try {
                window.show();
                TextField command = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                command.setText("list-participants E1");
                command.fireEvent(new ActionEvent());
                assertEquals(first, eventListView(window).getSelectionModel().getSelectedItem());
                assertEquals(1, participantListView(window).getItems().size());

                command.setText("view-event E2");
                command.fireEvent(new ActionEvent());
                assertEquals(second, model.getSelectedEvent().orElseThrow());
                assertEquals(second, eventListView(window).getSelectionModel().getSelectedItem());
                assertTrue(participantListView(window).getItems().isEmpty());

                command.setText("view-event E1");
                command.fireEvent(new ActionEvent());
                assertEquals(first, eventListView(window).getSelectionModel().getSelectedItem());
                assertEquals(Set.copyOf(model.getParticipationsForEvent(first)),
                        Set.copyOf(participantListView(window).getItems()));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void actionButtons_loaded_haveVisibleLabelsAndActions() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(new ModelManager());
            try {
                Button addPersonButton = (Button) window.getPrimaryStage().getScene().lookup("#addPersonButton");
                Button addEventButton = (Button) window.getPrimaryStage().getScene().lookup("#addEventButton");

                assertNotNull(addPersonButton);
                assertNotNull(addEventButton);
                assertEquals("+ Person", addPersonButton.getText());
                assertEquals("+ Event", addEventButton.getText());
                assertNotNull(addPersonButton.getOnAction());
                assertNotNull(addEventButton.getOnAction());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addPersonButton_submitValidForm_addsPerson() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                window.show();
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitForm(failure,
                        "Full name", "Alice Tan",
                        "Phone number", "91234567",
                        "Email address", "alice@example.com",
                        "Home address", "1 Main Street"));

                Button addPersonButton = (Button) window.getPrimaryStage().getScene().lookup("#addPersonButton");
                addPersonButton.fire();

                throwFailure(failure);
                assertEquals(1, model.getFilteredPersonList().size());
                assertEquals("Alice Tan", model.getFilteredPersonList().getFirst().getName().fullName);
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addEventButton_submitValidForm_addsEvent() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            MainWindow window = createWindow(model);
            try {
                window.show();
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitForm(failure,
                        "Event name", "Workshop",
                        "Maximum participants", "20",
                        "Event description", "Learn Java"));

                Button addEventButton = (Button) window.getPrimaryStage().getScene().lookup("#addEventButton");
                addEventButton.fire();

                throwFailure(failure);
                assertEquals(1, model.getFilteredEventList().size());
                assertEquals("Workshop", model.getFilteredEventList().getFirst().getName());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void personRowDeleteButton_confirmDeletion_deletesPerson() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            MainWindow window = createWindow(model);
            try {
                window.show();
                ListView<Person> list = personListView(window.getPersonListPanel());
                ListCell<Person> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> clickDialogButton(failure, "Delete"));

                Button deleteButton = (Button) cell.getGraphic().lookup("#deleteButton");
                deleteButton.fire();

                throwFailure(failure);
                assertTrue(model.getFilteredPersonList().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void personRowEditButton_submitValidForm_editsPerson() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            MainWindow window = createWindow(model);
            try {
                window.show();
                ListView<Person> list = personListView(window.getPersonListPanel());
                ListCell<Person> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitFormAndClick(failure, "Edit",
                        "Full name", "Alice Edited",
                        "Phone number", "98765432",
                        "Email address", "edited@example.com",
                        "Home address", "2 New Street",
                        "friend, volunteer", "colleague, organiser"));

                Button editButton = (Button) cell.getGraphic().lookup("#editButton");
                editButton.fire();

                throwFailure(failure);
                Person editedPerson = model.getFilteredPersonList().getFirst();
                assertEquals("Alice Edited", editedPerson.getName().fullName);
                assertEquals("98765432", editedPerson.getPhone().value);
                assertEquals("edited@example.com", editedPerson.getEmail().value);
                assertEquals("2 New Street", editedPerson.getAddress().value);
                assertEquals(Set.of(new Tag("colleague"), new Tag("organiser")), editedPerson.getTags());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void personRowDeleteButton_staleEntry_displaysCommandError() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(ALICE);
            MainWindow window = createWindow(model);
            try {
                window.show();
                ListView<Person> list = personListView(window.getPersonListPanel());
                ListCell<Person> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                Button staleDeleteButton = (Button) cell.getGraphic().lookup("#deleteButton");
                model.deletePerson(ALICE);
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> clickDialogButton(failure, "Delete"));

                staleDeleteButton.fire();

                throwFailure(failure);
                TextArea resultDisplay = (TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay");
                assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, resultDisplay.getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void eventRowButtons_addParticipantThenDeleteEvent_updatesModel() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            model.addPerson(ALICE);
            model.addEvent(event);
            MainWindow window = createWindow(model);
            try {
                window.show();
                TabPane listTabs = (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
                listTabs.getSelectionModel().select(1);
                window.getPrimaryStage().getScene().getRoot().applyCss();
                ListView<Event> list = eventListView(window);
                ListCell<Event> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);

                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitForm(failure,
                        "e.g. P1", "P1",
                        "Facilitator, Guest Speaker", "Facilitator, Guest Speaker",
                        "VIP, Alumni", "VIP, Alumni"));
                Button addParticipantButton = (Button) cell.getGraphic().lookup("#addParticipantButton");
                addParticipantButton.fire();

                throwFailure(failure);
                assertEquals(1, model.getParticipationsForEvent(event).size());
                assertEquals(Set.of(new ParticipationRole("Facilitator"), new ParticipationRole("Guest Speaker")),
                        model.getParticipationsForEvent(event).iterator().next().getRoles());
                assertEquals(Set.of(new Tag("VIP"), new Tag("Alumni")),
                        model.getParticipationsForEvent(event).iterator().next().getTags());

                Platform.runLater(() -> clickDialogButton(failure, "Delete"));
                Button deleteButton = (Button) cell.getGraphic().lookup("#deleteButton");
                deleteButton.fire();

                throwFailure(failure);
                assertTrue(model.getFilteredEventList().isEmpty());
                assertTrue(model.getParticipations().getParticipations().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void eventRowEditButton_submitValidForm_editsEvent() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            model.addEvent(event);
            MainWindow window = createWindow(model);
            try {
                window.show();
                TabPane listTabs = (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
                listTabs.getSelectionModel().select(1);
                ListView<Event> list = eventListView(window);
                ListCell<Event> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitFormAndClick(failure, "Edit",
                        "Event name", "Advanced Workshop",
                        "Maximum participants", "40",
                        "Event description", "Advanced Java"));

                Button editButton = (Button) cell.getGraphic().lookup("#editButton");
                editButton.fire();

                throwFailure(failure);
                Event editedEvent = model.getFilteredEventList().getFirst();
                assertEquals("E1", editedEvent.getId());
                assertEquals("Advanced Workshop", editedEvent.getName());
                assertEquals(40, editedEvent.getCapacity());
                assertEquals("Advanced Java", editedEvent.getDescription());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void eventRowArchiveButton_togglesArchiveState() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            assertEquals("archive-event E1", MainWindow.buildArchiveCommand(event));
            assertEquals("unarchive-event E1", MainWindow.buildArchiveCommand(event.withArchived(true)));
            ModelManager activeModel = new ModelManager();
            activeModel.addEvent(event);
            MainWindow activeWindow = createWindow(activeModel);
            try {
                activeWindow.show();
                ListView<Event> list = eventListView(activeWindow);
                ListCell<Event> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                Button archiveButton = (Button) cell.getGraphic().lookup("#archiveButton");

                assertEquals("Archive", archiveButton.getText());
                archiveButton.fire();

                assertTrue(activeModel.getEvents().getFirst().isArchived());
            } finally {
                activeWindow.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void participantRowButtons_editThenDelete_updatesParticipation() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            model.addPerson(ALICE);
            model.addEvent(event);
            model.addParticipation(event, ALICE);
            MainWindow window = createWindow(model);
            try {
                window.show();
                Participation participation = model.getParticipationsForEvent(event).iterator().next();
                window.getParticipantListPanel().showParticipantsForEvent(event, Set.of(participation));
                ListView<Participation> list = participantListView(window);
                ListCell<Participation> cell = list.getCellFactory().call(list);
                cell.updateListView(list);
                cell.updateIndex(0);
                AtomicReference<Throwable> failure = new AtomicReference<>();
                Platform.runLater(() -> submitFormAndClick(failure, "Edit",
                        "Facilitator, Guest Speaker", "Speaker, Host",
                        "VIP, Alumni", "VIP"));

                Button editButton = (Button) cell.getGraphic().lookup("#editButton");
                editButton.fire();

                throwFailure(failure);
                Participation edited = model.getParticipationsForEvent(event).iterator().next();
                assertEquals(Set.of(new ParticipationRole("Speaker"), new ParticipationRole("Host")),
                        edited.getRoles());
                assertEquals(Set.of(new Tag("VIP")), edited.getTags());

                Platform.runLater(() -> clickDialogButton(failure, "Delete"));
                Button deleteButton = (Button) cell.getGraphic().lookup("#deleteButton");
                deleteButton.fire();

                throwFailure(failure);
                assertTrue(model.getParticipationsForEvent(event).isEmpty());
                assertEquals(1, model.getFilteredPersonList().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow(ModelManager model) {
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("people.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("people.json"));
        window.fillInnerParts();
        return window;
    }

    private void submitForm(AtomicReference<Throwable> failure, String... promptAndValues) {
        submitFormAndClick(failure, "Add", promptAndValues);
    }

    private void submitFormAndClick(
            AtomicReference<Throwable> failure, String buttonText, String... promptAndValues) {
        try {
            DialogPane dialogPane = showingDialogPane();
            for (int i = 0; i < promptAndValues.length; i += 2) {
                String prompt = promptAndValues[i];
                TextField field = dialogPane.lookupAll(".text-field").stream()
                        .filter(TextField.class::isInstance)
                        .map(TextField.class::cast)
                        .filter(candidate -> prompt.equals(candidate.getPromptText()))
                        .findFirst()
                        .orElseThrow();
                field.setText(promptAndValues[i + 1]);
            }
            clickButton(dialogPane, buttonText);
        } catch (Throwable throwable) {
            failure.set(throwable);
            closeShowingDialog();
        }
    }

    private void clickDialogButton(AtomicReference<Throwable> failure, String buttonText) {
        try {
            clickButton(showingDialogPane(), buttonText);
        } catch (Throwable throwable) {
            failure.set(throwable);
            closeShowingDialog();
        }
    }

    private void clickButton(DialogPane dialogPane, String buttonText) {
        ButtonType buttonType = dialogPane.getButtonTypes().stream()
                .filter(candidate -> buttonText.equals(candidate.getText()))
                .findFirst()
                .orElseThrow();
        Button button = (Button) dialogPane.lookupButton(buttonType);
        button.fire();
    }

    private DialogPane showingDialogPane() {
        return Window.getWindows().stream()
                .filter(Window::isShowing)
                .map(Window::getScene)
                .filter(scene -> scene != null)
                .map(scene -> scene.getRoot())
                .filter(DialogPane.class::isInstance)
                .map(DialogPane.class::cast)
                .findFirst()
                .orElseThrow();
    }

    private void closeShowingDialog() {
        Window.getWindows().stream()
                .filter(Window::isShowing)
                .filter(window -> window.getScene() != null)
                .filter(window -> window.getScene().getRoot() instanceof DialogPane)
                .forEach(Window::hide);
    }

    private void throwFailure(AtomicReference<Throwable> failure) {
        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    @SuppressWarnings("unchecked")
    private ListView<Person> personListView(PersonListPanel panel) {
        return (ListView<Person>) panel.getRoot().lookup("#personListView");
    }

    @SuppressWarnings("unchecked")
    private ListView<Event> eventListView(MainWindow window) {
        return (ListView<Event>) window.getPrimaryStage().getScene().lookup("#eventListView");
    }

    @SuppressWarnings("unchecked")
    private ListView<Participation> participantListView(MainWindow window) {
        return (ListView<Participation>) window.getPrimaryStage().getScene().lookup("#participantListView");
    }
}
