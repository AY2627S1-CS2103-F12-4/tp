package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ParticipantSearchWindowTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void searchThroughCommandBox_matchesAndEmptyResults_selectEventsTab() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-participant John /event E1");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals(1, participants(window).getItems().size());
                assertEquals("birthday (E1)", heading(window, "eventHeading"));
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
                assertEquals(1, events(window).getItems().size());
                assertEquals("1 matching registration across 1 event for \"John\".", feedback(window));
                enterCommand(window, "find-event birthday");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                enterCommand(window, "search-participant Missing /event E1");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertTrue(participants(window).getItems().isEmpty());
                assertTrue(events(window).getItems().isEmpty());
                assertEquals("No matching registrations for \"Missing\" in nonarchived events.", feedback(window));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void invalidSearch_preservesSelectedTabAndPreviousResults() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-participant John /event E1");
                tabs(window).getSelectionModel().select(2);
                enterCommand(window, "search-participant John /event E99");
                assertEquals("Participants", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals(1, participants(window).getItems().size());
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
                enterCommand(window, "search-participant John /event");
                assertEquals("Participants", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals(1, participants(window).getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteMembershipThroughCommandBox_updatesDisplayedList() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-participant John /event E1");
                enterCommand(window, "del-participant E1 P1");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertTrue(participants(window).getItems().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteSearchedEvent_resetsParticipantContextAndSelectsEvents() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-participant John /event E1");
                enterCommand(window, "del-event E1");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participant search", heading(window, "eventHeading"));
                assertEquals("Use search-participant NAME [/event EVENTID]", heading(window, "searchHeading"));
                enterCommand(window, "find-event birthday");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void globalSearch_switchingTabsPreservesSearchAndListEventsClearsIt() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-participant John   Doe");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals("All nonarchived events", heading(window, "eventHeading"));
                assertEquals("Matching \"John Doe\"", heading(window, "searchHeading"));
                assertEquals(1, participants(window).getItems().size());
                assertEquals(1, events(window).getItems().size());

                tabs(window).getSelectionModel().select(2);
                tabs(window).getSelectionModel().select(1);
                assertEquals(1, participants(window).getItems().size());
                assertEquals("Matching \"John Doe\"", heading(window, "searchHeading"));

                enterCommand(window, "list-event");
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participant search", heading(window, "eventHeading"));
                assertEquals(1, events(window).getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow() {
        ModelManager model = new ModelManager();
        Event event = new Event(new EventId("E1"), "birthday", 30, "Party");
        Person person = new PersonBuilder().withId("P1").withName("John Doe").build();
        model.addEvent(event);
        model.addPerson(person);
        model.addParticipation(event, person);
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("people.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("people.json"));
        window.fillInnerParts();
        return window;
    }

    private void enterCommand(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private TabPane tabs(MainWindow window) {
        return (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
    }

    private ListView<?> participants(MainWindow window) {
        return (ListView<?>) tabs(window).getTabs().get(2).getContent().lookup("#participantListView");
    }

    private ListView<?> events(MainWindow window) {
        return (ListView<?>) tabs(window).getTabs().get(1).getContent().lookup("#eventListView");
    }

    private String feedback(MainWindow window) {
        return ((TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay")).getText();
    }

    private String heading(MainWindow window, String id) {
        return ((Label) tabs(window).getTabs().get(2).getContent().lookup("#" + id)).getText();
    }
}
