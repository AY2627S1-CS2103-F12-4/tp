package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.ui.SearchUiTestUtil.enterCommand;
import static seedu.address.ui.SearchUiTestUtil.getEvents;
import static seedu.address.ui.SearchUiTestUtil.getFeedback;
import static seedu.address.ui.SearchUiTestUtil.getHeading;
import static seedu.address.ui.SearchUiTestUtil.getParticipants;
import static seedu.address.ui.SearchUiTestUtil.getSelectedTabName;
import static seedu.address.ui.SearchUiTestUtil.getTabs;
import static seedu.address.ui.SearchUiTestUtil.prepareEventTab;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
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
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals("birthday (E1)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"John\"", getHeading(window, "searchHeading"));
                assertEquals(1, getEvents(window).getItems().size());
                assertEquals("1 matching registration across 1 event for \"John\".", getFeedback(window));
                enterCommand(window, "find-event birthday");
                assertEquals("Events", getSelectedTabName(window));
                enterCommand(window, "search-participant Missing /event E1");
                assertEquals("Events", getSelectedTabName(window));
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertTrue(getEvents(window).getItems().isEmpty());
                assertEquals("No matching registrations for \"Missing\" in nonarchived events.", getFeedback(window));
                assertEquals("Matching \"Missing\"", getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clearSearchButton_afterParticipantAndRoleSearch_runsListEvent() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                Button clearSearchButton = (Button) window.getPrimaryStage()
                        .getScene()
                        .lookup("#clearSearchButton");

                assertNotNull(clearSearchButton);
                assertFalse(clearSearchButton.isVisible());

                for (String command : new String[]{
                    "search-participant John",
                    "search-role Missing"
                }) {
                    enterCommand(window, command);
                    assertTrue(clearSearchButton.isVisible());

                    clearSearchButton.fire();

                    assertFalse(clearSearchButton.isVisible());
                    assertEquals("Listing events...", getFeedback(window));
                    assertEquals(1, getEvents(window).getItems().size());
                    assertTrue(getParticipants(window).getItems().isEmpty());
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void listParticipants_targetOutsideNameSearch_resetsSearchAndDisplaysRegistrations() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "add-event Main /cap 10 /desc Main event");
                enterCommand(window, "add-event Other /cap 10 /desc Other event");
                enterCommand(window, "add-participant E1 P1 /role Host");
                enterCommand(window, "add-participant E2 P1 /role Speaker");
                enterCommand(window, "search-participant Alex /event E1");
                assertEquals(List.of("E1"), getEvents(window).getItems().stream().map(Event::getId).toList());
                Button clearSearchButton = (Button) window.getPrimaryStage().getScene().lookup("#clearSearchButton");
                assertTrue(clearSearchButton.isVisible());

                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "list-participants E2");

                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of("E1", "E2"), getEvents(window).getItems().stream().map(Event::getId).toList());
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Other (E2)", getHeading(window, "eventHeading"));
                assertEquals("Participants", getHeading(window, "searchHeading"));
                assertEquals(1, getParticipants(window).getItems().size());
                Participation displayed = getParticipants(window).getItems().getFirst();
                assertEquals("E2", displayed.getEvent().getId());
                assertEquals("P1", displayed.getPerson().getId().toString());
                assertEquals("Alex Yeoh", displayed.getPerson().getName().fullName);
                assertEquals(Set.of(new ParticipationRole("Speaker")), displayed.getRoles());
                assertEquals("Listing participants...", getFeedback(window));
                assertFalse(clearSearchButton.isVisible());
                assertFalse(clearSearchButton.isManaged());

                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Participants", getHeading(window, "searchHeading"));
                assertEquals(Set.of(new ParticipationRole("Guest")),
                        getParticipants(window).getItems().getFirst().getRoles());
                assertFalse(clearSearchButton.isVisible());

                enterCommand(window, "search-participant Alex /event E1");
                assertEquals(List.of("E1"), getEvents(window).getItems().stream().map(Event::getId).toList());
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Matching \"Alex\"", getHeading(window, "searchHeading"));
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals("E1", getParticipants(window).getItems().getFirst().getEvent().getId());
                assertTrue(clearSearchButton.isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void listParticipants_afterEmptySearch_displaysEmptyAndArchivedTargets() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "add-event Empty /cap 10 /desc No registrations");
                enterCommand(window, "add-event Archived /cap 10 /desc Archived event");
                enterCommand(window, "add-participant E3 P1 /role Host");
                enterCommand(window, "archive-event E3");
                assertEquals(List.of("E1", "E2"), getEvents(window).getItems().stream().map(Event::getId).toList());

                for (String target : List.of("E2", "E3")) {
                    enterCommand(window, "search-participant Missing /event E1");
                    assertTrue(getEvents(window).getItems().isEmpty());

                    enterCommand(window, "list-participants " + target);

                    assertEquals("Events", getSelectedTabName(window));
                    assertEquals(target, getEvents(window).getSelectionModel().getSelectedItem().getId());
                    assertEquals("Participants", getHeading(window, "searchHeading"));
                    Button clearSearchButton = (Button) window.getPrimaryStage()
                            .getScene().lookup("#clearSearchButton");
                    assertFalse(clearSearchButton.isVisible());
                    assertFalse(clearSearchButton.isManaged());
                    if (target.equals("E2")) {
                        assertEquals(List.of("E1", "E2"),
                                getEvents(window).getItems().stream().map(Event::getId).toList());
                        assertEquals("Empty (E2)", getHeading(window, "eventHeading"));
                        assertTrue(getParticipants(window).getItems().isEmpty());
                        Label placeholder = (Label) getParticipants(window).getPlaceholder();
                        assertEquals("No participants found for this event.", placeholder.getText());
                    } else {
                        assertEquals(List.of("E1", "E2", "E3"),
                                getEvents(window).getItems().stream().map(Event::getId).toList());
                        assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
                        assertEquals(1, getParticipants(window).getItems().size());
                        assertEquals("E3", getParticipants(window).getItems().getFirst().getEvent().getId());
                    }
                }
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
                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-participant John /event E99");
                assertEquals("People", getSelectedTabName(window));
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals("Matching \"John\"", getHeading(window, "searchHeading"));
                enterCommand(window, "search-participant John /event");
                assertEquals("People", getSelectedTabName(window));
                assertEquals(1, getParticipants(window).getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void selectingEvent_displaysItsParticipants()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                getTabs(window).getSelectionModel().select(1);
                getEvents(window).getSelectionModel().select(0);

                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals(
                        "birthday (E1)",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Participants",
                        getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void clearingEventSelection_clearsParticipantPanel()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                getTabs(window).getSelectionModel().select(1);
                ListView<Event> eventList = getEvents(window);

                eventList.getSelectionModel().select(0);
                eventList.getSelectionModel().clearSelection();

                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(
                        "Participants",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Select an event to view its participants.",
                        getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteSelectedMembership_refreshesParticipantList()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                getTabs(window).getSelectionModel().select(1);
                getEvents(window).getSelectionModel().select(0);

                assertEquals(1, getParticipants(window).getItems().size());

                enterCommand(window, "del-participant E1 P1");

                assertEquals("Events", getSelectedTabName(window));
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(
                        "birthday (E1)",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Participants",
                        getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteSelectedEvent_clearsParticipantPanel()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                getTabs(window).getSelectionModel().select(1);
                ListView<Event> eventList = getEvents(window);

                eventList.getSelectionModel().select(0);

                assertEquals(1, getParticipants(window).getItems().size());

                enterCommand(window, "del-event E1");

                assertEquals("Events", getSelectedTabName(window));
                assertTrue(eventList.getItems().isEmpty());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(
                        "Participants",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Select an event to view its participants.",
                        getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void selectingEventAfterClearingSearch_listOpensPeopleAndPreservesParticipants()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                enterCommand(
                        window,
                        "search-participant Missing /event E1");

                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(
                        "Matching \"Missing\"",
                        getHeading(window, "searchHeading"));

                enterCommand(window, "list-event");
                getEvents(window).getSelectionModel().select(0);

                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals(
                        "Participants",
                        getHeading(window, "searchHeading"));

                enterCommand(window, "list");

                assertEquals("People", getSelectedTabName(window));
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals(
                        "birthday (E1)",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Participants",
                        getHeading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteMembershipFromSearch_updatesDisplayedResults()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                enterCommand(
                        window,
                        "search-participant John /event E1");
                enterCommand(window, "del-participant E1 P1");

                assertEquals("Events", getSelectedTabName(window));
                assertTrue(getParticipants(window).getItems().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deleteSearchedEvent_resetsParticipantContext()
            throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();

            try {
                enterCommand(
                        window,
                        "search-participant John /event E1");
                enterCommand(window, "del-event E1");

                assertEquals("Events", getSelectedTabName(window));
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(
                        "Participants",
                        getHeading(window, "eventHeading"));
                assertEquals(
                        "Select an event to view its participants.",
                        getHeading(window, "searchHeading"));
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
                assertEquals("Events", getSelectedTabName(window));
                assertEquals("birthday (E1)", getHeading(window, "eventHeading"));
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Matching \"John Doe\"", getHeading(window, "searchHeading"));
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals(1, getEvents(window).getItems().size());

                getTabs(window).getSelectionModel().select(0);
                getTabs(window).getSelectionModel().select(1);
                assertEquals(1, getParticipants(window).getItems().size());
                assertEquals("Matching \"John Doe\"", getHeading(window, "searchHeading"));

                enterCommand(window, "list-event");
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Participants", getHeading(window, "eventHeading"));
                assertEquals(1, getEvents(window).getItems().size());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow() {
        ModelManager model = new ModelManager();

        Event event = new Event(
                new EventId("E1"),
                "birthday",
                30,
                "Party");

        Person person = new PersonBuilder()
                .withId("P1")
                .withName("John Doe")
                .build();

        model.addEvent(event);
        model.addPerson(person);
        model.addParticipation(event, person);

        return createWindow(model);
    }

    private MainWindow createWindow(ModelManager model) {
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(
                        temporaryFolder.resolve("people.json")),
                new JsonUserPrefsStorage(
                        temporaryFolder.resolve("prefs.json")),
                new JsonParticipationStorage(
                        temporaryFolder.resolve(
                                "participations.json")));

        Logic logic = new LogicManager(model, storage);

        MainWindow window = new MainWindow(
                new Stage(),
                logic,
                temporaryFolder.resolve("people.json"));

        window.fillInnerParts();
        prepareEventTab(window);
        return window;
    }
}
