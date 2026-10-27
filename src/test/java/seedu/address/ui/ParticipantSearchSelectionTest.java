package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class ParticipantSearchSelectionTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void globalSearch_selectsFirstMatchAndEventClicksShowOnlyItsMatches() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John");
                assertEquals(List.of("E1", "E2"), eventIds(window));
                assertSelection(window, "E1", "P1");
                assertTrue(model.getParticipantSearchEvent().isEmpty());

                events(window).getSelectionModel().select(1);
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("Reception (E2)", heading(window, "eventHeading"));
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
                assertEquals(Set.of(new ParticipationRole("Guest")),
                        participants(window).getItems().getFirst().getRoles());

                events(window).getSelectionModel().select(0);
                assertSelection(window, "E1", "P1");
                assertEquals("John", model.getParticipantSearchKeyword().orElseThrow());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void newSearch_selectsFirstMatchAndScopedSearchSelectsItsEvent() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "search-participant John");
                assertSelection(window, "E1", "P1");

                enterCommand(window, "search-participant John /event E2");
                assertEquals(List.of("E2"), eventIds(window));
                assertSelection(window, "E2", "P1", "P3");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void invalidSearch_preservesEventSelectionRowsAndSelectedTab() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                tabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-participant John /event E99");
                assertEquals("People", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertSelection(window, "E2", "P1", "P3");
                enterCommand(window, "search-participant John /event");
                assertEquals("People", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void noMatches_clearsSelectionAndRowsButRetainsGlobalOrArchivedScope() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John");
                enterCommand(window, "search-participant Missing");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertTrue(events(window).getItems().isEmpty());
                assertEquals("All nonarchived events", heading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
                enterCommand(window, "list");
                assertEquals("Missing", model.getParticipantSearchKeyword().orElseThrow());

                enterCommand(window, "search-participant John /event E3");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                Event archived = model.getParticipantSearchEvent().orElseThrow();
                assertEquals(archived.getDisplayName() + " (E3)", heading(window, "eventHeading"));
                enterCommand(window, "unarchive-event E3");
                assertSelection(window, "E3", "P1");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void registrationChanges_refreshSelectedMatchesAndChooseNextEvent() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "del-participant E2 P3");
                assertSelection(window, "E2", "P1");
                assertEquals(List.of("E1", "E2"), eventIds(window));
                enterCommand(window, "add-participant E2 P3 /role Host");
                assertSelection(window, "E2", "P1", "P3");
                enterCommand(window, "del-participant E2 P1");
                assertSelection(window, "E2", "P3");
                enterCommand(window, "del-participant E2 P3");
                assertSelection(window, "E1", "P1");
                enterCommand(window, "del-participant E1 P1");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void participantEdits_refreshRowsWhenMatchingEventsStayTheSame() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "edit-participant E2 P1 /role Host /tag VIP");
                assertSelection(window, "E2", "P1", "P3");
                Participation changed = participants(window).getItems().getFirst();
                assertEquals(Set.of(new ParticipationRole("Host")), changed.getRoles());
                assertEquals(Set.of(new Tag("VIP")), changed.getTags());
                enterCommand(window, "mark-participant E2 P1");
                assertTrue(participants(window).getItems().getFirst().isPresent());
                enterCommand(window, "edit 1 n/John Updated");
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("John Updated", participants(window).getItems().getFirst().getPerson().getName().fullName);
                assertEquals(List.of("E1", "E2"), eventIds(window));
                enterCommand(window, "delete 1");
                assertSelection(window, "E2", "P3");
                assertEquals(List.of("E2"), eventIds(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void eventEditsAndArchiving_preserveSelectionOrChooseFirstRemainingMatch() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "edit-event E1 Updated Wedding");
                assertSelection(window, "E2", "P1", "P3");
                enterCommand(window, "edit-event E2 Updated Reception");
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("Updated Reception (E2)", heading(window, "eventHeading"));
                enterCommand(window, "archive-event E2");
                assertSelection(window, "E1", "P1");
                enterCommand(window, "unarchive-event E2");
                assertSelection(window, "E1", "P1");
                assertEquals(List.of("E1", "E2"), eventIds(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deletingEvents_refreshesGlobalSearchAndClearsDeletedScopedSearch() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "del-event E2");
                assertSelection(window, "E1", "P1");
                enterCommand(window, "search-participant John /event E1");
                enterCommand(window, "del-event E1");
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertEquals(List.of("E4"), eventIds(window));
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participants", heading(window, "eventHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void findEvent_afterListingParticipants_clearsPanelUntilEventReselected() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "list-participants E1");
                assertSelection(window, "E1", "P1", "P2");

                enterCommand(window, "find-event Wedding");

                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals(List.of("E1"), eventIds(window));
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participants", heading(window, "eventHeading"));
                assertEquals("Select an event to view its participants.", heading(window, "searchHeading"));
                assertTrue(model.getParticipantSearchKeyword().isEmpty());

                events(window).getSelectionModel().select(0);

                assertSelection(window, "E1", "P1", "P2");
                assertEquals("Wedding (E1)", heading(window, "eventHeading"));
                assertEquals("Participants", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void listEvent_afterEventSelection_clearsPanelWithAndWithoutArchives() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                for (String command : List.of("list-event", "list-event /include-archive")) {
                    events(window).getSelectionModel().select(0);
                    assertSelection(window, "E1", "P1", "P2");

                    enterCommand(window, command);

                    List<String> expectedEventIds = command.equals("list-event")
                            ? List.of("E1", "E2", "E4") : List.of("E1", "E2", "E3", "E4");
                    assertEquals(expectedEventIds, eventIds(window));
                    assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                    assertNull(events(window).getSelectionModel().getSelectedItem());
                    assertTrue(participants(window).getItems().isEmpty());
                    assertEquals("Participants", heading(window, "eventHeading"));
                    assertEquals("Select an event to view its participants.", heading(window, "searchHeading"));

                    events(window).getSelectionModel().select(0);

                    assertSelection(window, "E1", "P1", "P2");
                    assertEquals("Wedding (E1)", heading(window, "eventHeading"));
                    assertEquals("Participants", heading(window, "searchHeading"));
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void resetCommands_restoreOrdinaryEventSelectionAndDoNotReviveSearch() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John");
                enterCommand(window, "find-event Wedding");
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertEquals(List.of("E1"), eventIds(window));
                assertTrue(participants(window).getItems().isEmpty());
                events(window).getSelectionModel().select(0);
                assertSelection(window, "E1", "P1", "P2");
                enterCommand(window, "list");
                assertSelection(window, "E1", "P1", "P2");
                assertEquals("Participants", heading(window, "searchHeading"));

                enterCommand(window, "search-participant John /event E2");
                enterCommand(window, "list-event");
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertEquals(List.of("E1", "E2", "E4"), eventIds(window));
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void modelResultNotifications_refreshSnapshotsWithoutExecutingCommands() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John");
                events(window).getSelectionModel().select(1);
                model.editParticipation(new EventId("E2"), new PersonId("P1"),
                        Set.of(new ParticipationRole("Speaker")), null);
                assertSelection(window, "E2", "P1", "P3");
                assertEquals(Set.of(new ParticipationRole("Speaker")),
                        participants(window).getItems().getFirst().getRoles());
                Person original = model.getFilteredPersonList().get(2);
                model.setPerson(original, new PersonBuilder(original).withName("Alice Tan").build());
                assertSelection(window, "E2", "P1");
                assertEquals(List.of("E1", "E2"), eventIds(window));
                model.updateEvent(new Event(new EventId("E1"), "Renamed Wedding", 30, "Updated event"));
                assertSelection(window, "E2", "P1");
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void newMatchesAfterEmptySearch_selectMatchingEventAutomatically() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant Missing");
                Person original = model.getFilteredPersonList().get(1);
                model.setPerson(original, new PersonBuilder(original).withName("Missing Person").build());
                assertSelection(window, "E1", "P2");
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveSearch_selectsMatchingEventsAndPreservesArchivedScope() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John /include-archive");
                assertEquals("Events", tabs(window).getSelectionModel().getSelectedItem().getText());
                assertEquals(List.of("E1", "E2", "E3"), eventIds(window));
                assertSelection(window, "E1", "P1");

                events(window).getSelectionModel().select(2);
                assertSelection(window, "E3", "P1");
                assertEquals("Archived <archived> (E3)", heading(window, "eventHeading"));
                assertEquals("John Doe", participants(window).getItems().getFirst().getPerson().getName().fullName);
                tabs(window).getSelectionModel().select(0);
                tabs(window).getSelectionModel().select(1);
                assertSelection(window, "E3", "P1");
                events(window).getSelectionModel().select(1);
                assertSelection(window, "E2", "P1", "P3");

                enterCommand(window, "search-participant John /event E3 /include-archive");
                assertEquals(List.of("E3"), eventIds(window));
                assertSelection(window, "E3", "P1");
                enterCommand(window, "search-participant John /event E3");
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("Archived <archived> (E3)", heading(window, "eventHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveSearch_emptyGlobalResults_useRequestedScopeAndRefreshArchivedMatches() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant Missing /include-archive");
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("All events", heading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
                enterCommand(window, "list");
                assertEquals("All events", heading(window, "eventHeading"));

                Event archived = new Event(new EventId("E5"), "Old Workshop", 10, "", true);
                Person missing = new PersonBuilder().withId("P5").withName("Missing Person").build();
                model.addEvent(archived);
                model.addPerson(missing);
                model.addParticipation(archived, missing);
                assertSelection(window, "E5", "P5");
                assertEquals("Old Workshop <archived> (E5)", heading(window, "eventHeading"));
                model.removeParticipation(archived, missing);
                assertEquals("All events", heading(window, "eventHeading"));
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());

                enterCommand(window, "search-participant Missing");
                assertEquals("All nonarchived events", heading(window, "eventHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveSearch_eventAndRegistrationChanges_retainSelectionOrChooseNextMatch() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow(createModel());
            try {
                enterCommand(window, "search-participant John /include-archive");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "archive-event E2");
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("Reception <archived> (E2)", heading(window, "eventHeading"));
                assertEquals(List.of("E1", "E2", "E3"), eventIds(window));
                enterCommand(window, "unarchive-event E2");
                assertSelection(window, "E2", "P1", "P3");
                assertEquals("Reception (E2)", heading(window, "eventHeading"));

                enterCommand(window, "del-participant E2 P1");
                assertSelection(window, "E2", "P3");
                enterCommand(window, "del-participant E2 P3");
                assertSelection(window, "E1", "P1");
                enterCommand(window, "del-event E1");
                assertSelection(window, "E3", "P1");
                assertEquals("Archived <archived> (E3)", heading(window, "eventHeading"));
                enterCommand(window, "del-event E3");
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("All events", heading(window, "eventHeading"));
                assertEquals("Matching \"John\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveListing_clearsSearchAndAllowsOrdinaryArchivedEventSelection() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-participant John /event E3 /include-archive");
                enterCommand(window, "list-event /include-archive");
                assertEquals(List.of("E1", "E2", "E3", "E4"), eventIds(window));
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("Participants", heading(window, "eventHeading"));

                events(window).getSelectionModel().select(2);
                assertSelection(window, "E3", "P1");
                assertEquals("Archived <archived> (E3)", heading(window, "eventHeading"));
                assertEquals("Participants", heading(window, "searchHeading"));
                enterCommand(window, "list-event");
                assertEquals(List.of("E1", "E2", "E4"), eventIds(window));
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        Event first = new Event(new EventId("E1"), "Wedding", 30, "Ceremony");
        Event second = new Event(new EventId("E2"), "Reception", 30, "Dinner");
        Event archived = new Event(new EventId("E3"), "Archived", 30, "Old event", true);
        model.addEvent(first);
        model.addEvent(second);
        model.addEvent(archived);
        model.addEvent(new Event(new EventId("E4"), "Other", 30, "No participants"));
        Person john = new PersonBuilder().withId("P1").withName("John Doe").build();
        Person jane = new PersonBuilder().withId("P2").withName("Jane Doe").build();
        Person otherJohn = new PersonBuilder().withId("P3").withName("John Tan").build();
        model.addPerson(john);
        model.addPerson(jane);
        model.addPerson(otherJohn);
        model.addParticipation(first, john);
        model.addParticipation(first, jane);
        model.addParticipation(new EventId(second.getId()), john.getId(),
                Set.of(new ParticipationRole("Guest")), Set.of());
        model.addParticipation(second, otherJohn);
        model.addParticipation(archived, john);
        return model;
    }

    private MainWindow createWindow(ModelManager model) {
        Logic logic = new LogicManager(model, new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("people.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")),
                new JsonParticipationStorage(temporaryFolder.resolve("participations.json"))));
        MainWindow window = new MainWindow(new Stage(), logic, temporaryFolder.resolve("people.json"));
        window.fillInnerParts();
        tabs(window).getSelectionModel().select(1);
        window.getPrimaryStage().getScene().getRoot().applyCss();
        tabs(window).getSelectionModel().select(0);
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

    @SuppressWarnings("unchecked")
    private ListView<Event> events(MainWindow window) {
        return (ListView<Event>) tabs(window).getTabs().get(1).getContent().lookup("#eventListView");
    }

    @SuppressWarnings("unchecked")
    private ListView<Participation> participants(MainWindow window) {
        return (ListView<Participation>) window.getParticipantListPanel().getRoot().lookup("#participantListView");
    }

    private List<String> eventIds(MainWindow window) {
        return events(window).getItems().stream().map(Event::getId).toList();
    }

    private void assertSelection(MainWindow window, String eventId, String... personIds) {
        assertEquals(eventId, events(window).getSelectionModel().getSelectedItem().getId());
        List<Participation> displayed = participants(window).getItems();
        assertEquals(List.of(personIds), displayed.stream().map(row -> row.getPerson().getId().toString()).toList());
        assertTrue(displayed.stream().allMatch(row -> row.getEvent().getId().equals(eventId)));
    }

    private String heading(MainWindow window, String id) {
        return ((Label) window.getParticipantListPanel().getRoot().lookup("#" + id)).getText();
    }
}
