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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.mapper.ParticipationRole;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonId;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonParticipationStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class RoleSearchWindowTest {
    private static final Event CONFERENCE = new Event(new EventId("E1"), "Conference", 20, "");
    private static final Event WORKSHOP = new Event(new EventId("E2"), "Workshop", 20, "");
    private static final Event ARCHIVED = new Event(new EventId("E3"), "Archived", 20, "", true);
    private static final String SELECT_EVENT_PROMPT = "Select an event to view its participants.";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void addEventDuringSearch_preservesSelectedMatchAndHeadings() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                for (String search : List.of("search-role Speaker", "search-participant Alice",
                        "search-role Speaker /include-archive")) {
                    enterCommand(window, search);
                    List<Event> matchingEvents = List.copyOf(events(window).getItems());
                    Event selected = matchingEvents.getLast();
                    events(window).getSelectionModel().select(selected);
                    List<String> registrations = resultIds(window);
                    String eventHeading = heading(window, "eventHeading");
                    String searchHeading = heading(window, "searchHeading");
                    int eventCount = model.getEvents().size();

                    enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                    assertEquals("Event added successfully.", feedback(window));
                    assertEquals(eventCount + 1, model.getEvents().size());
                    assertEquals("Events", selectedTab(window));
                    assertEquals(matchingEvents, events(window).getItems());
                    assertEquals(selected, events(window).getSelectionModel().getSelectedItem());
                    assertEquals(registrations, resultIds(window));
                    assertEquals(eventHeading, heading(window, "eventHeading"));
                    assertEquals(searchHeading, heading(window, "searchHeading"));
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addEventDuringEmptySearch_preservesEmptySelectionAndContext() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                for (String search : List.of("search-role Missing", "search-role Missing /event E1 /include-archive",
                        "search-participant Missing /include-archive", "search-participant Missing /event E1")) {
                    enterCommand(window, search);
                    assertTrue(events(window).getItems().isEmpty());
                    String eventHeading = heading(window, "eventHeading");
                    int eventCount = model.getEvents().size();

                    enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                    assertEquals("Event added successfully.", feedback(window));
                    assertEquals(eventCount + 1, model.getEvents().size());
                    assertEquals("Events", selectedTab(window));
                    assertTrue(events(window).getItems().isEmpty());
                    assertNull(events(window).getSelectionModel().getSelectedItem());
                    assertTrue(participants(window).getItems().isEmpty());
                    assertEquals(eventHeading, heading(window, "eventHeading"));
                    assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void addEventAfterRoleSearch_issue179Sequence_showsOnlyMatchingEvent() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "add-event Main /cap 10 /desc Main event");
                enterCommand(window, "add-event Other /cap 10 /desc Other event");
                enterCommand(window, "add-participant E1 P1 /role Host");
                enterCommand(window, "add-participant E2 P1 /role Speaker");
                enterCommand(window, "search-role Speaker");
                Event matchingEvent = new Event(new EventId("E2"), "Other", 10, "Other event");
                assertEquals(List.of(matchingEvent), events(window).getItems());

                enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                assertEquals("Event added successfully.", feedback(window));
                assertEquals(List.of("E1", "E2", "E3"), model.getEvents().stream().map(Event::getId).toList());
                assertEquals(List.of(matchingEvent), events(window).getItems());
                assertEquals(matchingEvent, events(window).getSelectionModel().getSelectedItem());
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Other (E2)", heading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                assertEquals("Events", selectedTab(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void globalSearch_showsFilteredEventsAndDetailedFeedback() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role sPeAkEr");
                assertEquals("Events", selectedTab(window));
                assertEquals(List.of("People", "Events"), tabs(window).getTabs().stream()
                        .map(tab -> tab.getText()).toList());
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", heading(window, "eventHeading"));
                assertEquals("Matching \"sPeAkEr\"", heading(window, "searchHeading"));
                assertEquals("2 registrations across 2 events with roles matching \"sPeAkEr\".", feedback(window));
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(Set.of(new ParticipationRole("Speaker"), new ParticipationRole("Host")),
                        participants(window).getItems().getFirst().getRoles());
                events(window).getSelectionModel().select(1);
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Workshop (E2)", heading(window, "eventHeading"));
                assertEquals("Matching \"sPeAkEr\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void scopedSearch_andEmptyResults_keepContextAndSelectEvents() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role  Speaker  /event E1");
                assertEquals("Events", selectedTab(window));
                assertEquals(List.of(CONFERENCE), events(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", heading(window, "eventHeading"));
                assertEquals("1 registration across 1 event with roles matching \"Speaker\".", feedback(window));

                enterCommand(window, "archive-event E1");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals(CONFERENCE.withArchived(true).getName() + " (E1)", heading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E1");
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());

                enterCommand(window, "search-role Missing /event E1");
                assertEquals("Events", selectedTab(window));
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Conference (E1)", heading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
                assertEquals("No registrations with roles matching \"Missing\" in nonarchived events.",
                        feedback(window));
                assertNull(events(window).getSelectionModel().getSelectedItem());

                enterCommand(window, "search-role Missing");
                assertEquals("All nonarchived events", heading(window, "eventHeading"));
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                enterCommand(window, "search-role Speaker /event E3");
                assertEquals(ARCHIVED.getName() + " (E3)", heading(window, "eventHeading"));
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void invalidSearches_preservePreviousResultsContextAndSelectedTab() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker");
                events(window).getSelectionModel().select(1);
                tabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("Event E99 does not exist.", feedback(window));
                assertEquals("People", selectedTab(window));
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals("Workshop (E2)", heading(window, "eventHeading"));

                enterCommand(window, "search-role Speaker /event");
                assertTrue(feedback(window).contains(SearchRoleCommand.MESSAGE_USAGE));
                assertEquals("People", selectedTab(window));
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void switchingTabsAndResettingSearch_updatesInstructionsWithoutChangingLayout() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker");
                tabs(window).getSelectionModel().select(0);
                tabs(window).getSelectionModel().select(1);
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));

                enterCommand(window, "list-event");
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participants", heading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));
                assertEquals("Listing events...", feedback(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertTrue(participants(window).getItems().isEmpty());

                enterCommand(window, "search-role Speaker");
                assertEquals(2, participants(window).getItems().size());
                enterCommand(window, "find-event Workshop");
                assertEquals(List.of(WORKSHOP), events(window).getItems());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));
                assertEquals("Showing event search results for Workshop...", feedback(window));

                enterCommand(window, "find-event Archived /include-archive");
                assertEquals(List.of(ARCHIVED), events(window).getItems());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Showing event search results for Archived...", feedback(window));

                enterCommand(window, "find-event Missing /include-archive");
                assertTrue(events(window).getItems().isEmpty());
                assertEquals("No matching events found.", feedback(window));

                enterCommand(window, "find-event Archived");
                assertTrue(events(window).getItems().isEmpty());
                assertEquals("No matching events found.", feedback(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void mutationsAndSearchTypeChanges_refreshRowsAndHeadings() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker");
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals(List.of(WORKSHOP), events(window).getItems());
                assertEquals("Workshop (E2)", heading(window, "eventHeading"));
                enterCommand(window, "archive-event E2");
                assertTrue(participants(window).getItems().isEmpty());
                assertTrue(events(window).getItems().isEmpty());
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E2");
                assertEquals(List.of("E2/P1"), resultIds(window));

                enterCommand(window, "search-participant Alice /event E1");
                assertEquals("Events", selectedTab(window));
                assertEquals("Conference (E1)", heading(window, "eventHeading"));
                assertEquals("Matching \"Alice\"", heading(window, "searchHeading"));
                assertEquals("1 matching registration across 1 event for \"Alice\".", feedback(window));
                enterCommand(window, "search-role Speaker /event E1");
                assertTrue(participants(window).getItems().isEmpty());
                enterCommand(window, "edit-participant E1 P1 /role Speaker");
                assertEquals("Events", selectedTab(window));
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", heading(window, "eventHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void selectionAndRoleChanges_preserveMatchingSelectionOrChooseNextEvent() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            addWorkshopSpeaker(model);
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-role Speaker");
                events(window).getSelectionModel().select(1);
                assertEquals(List.of("E2/P1", "E2/P4"), resultIds(window));
                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals("E2", events(window).getSelectionModel().getSelectedItem().getId());
                enterCommand(window, "edit-event E2 Renamed Workshop");
                assertEquals("Renamed Workshop (E2)", heading(window, "eventHeading"));
                assertEquals(List.of("E2/P4"), resultIds(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertEquals(List.of("E2/P4"), resultIds(window));
                enterCommand(window, "del-participant E2 P4");
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of("E1/P2"), resultIds(window));
                enterCommand(window, "del-participant E1 P2");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("All nonarchived events", heading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void observableRoleResults_refreshSelectedSnapshotWithoutCommands() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            Person dana = addWorkshopSpeaker(model);
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-role Speaker");
                events(window).getSelectionModel().select(1);
                model.editParticipation(new EventId("E2"), new PersonId("P1"),
                        Set.of(new ParticipationRole("Guest")), null);
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals("E2", events(window).getSelectionModel().getSelectedItem().getId());
                model.setPerson(dana, new PersonBuilder(dana).withName("Dana Updated").build());
                assertEquals("Dana Updated", participants(window).getItems().getFirst().getPerson().getName().fullName);
                model.markPresent(new EventId("E2"), dana.getId());
                assertTrue(participants(window).getItems().getFirst().isPresent());
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void deletingScopedOrGlobalEvents_updatesSearchContextAndSelection() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-role Speaker");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "del-event E2");
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Speaker", model.getParticipantSearchKeyword().orElseThrow());
                enterCommand(window, "search-role Speaker /event E1");
                enterCommand(window, "del-event E1");
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participants", heading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E3");
                assertTrue(participants(window).getItems().isEmpty());
                events(window).getSelectionModel().select(0);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Participants", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void switchingIdenticalQueries_replacesPredicateAndFailedSearchPreservesIt() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            Person bob = model.getFilteredPersonList().get(1);
            model.setPerson(bob, new PersonBuilder(bob).withName("Speaker").build());
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E1/P1"), resultIds(window));
                enterCommand(window, "search-participant Speaker");
                assertEquals(List.of("E1/P2"), resultIds(window));
                assertEquals("1 matching registration across 1 event for \"Speaker\".", feedback(window));
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("Event E99 does not exist.", feedback(window));
                enterCommand(window, "edit-participant E1 P1 /role Speaker");
                assertEquals(List.of("E1/P2"), resultIds(window));

                enterCommand(window, "search-role Speaker");
                assertEquals("2 registrations across 2 events with roles matching \"Speaker\".", feedback(window));
                assertEquals(List.of("E1/P1"), resultIds(window));
                enterCommand(window, "search-participant Speaker /event E99");
                assertEquals("Event E99 does not exist.", feedback(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void participantCommands_showAllEventParticipantsAndAllowRoleSearchToResume() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker");
                tabs(window).getSelectionModel().select(0);
                enterCommand(window, "list-participants E1");
                assertEquals("Events", selectedTab(window));
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals("Participants", heading(window, "searchHeading"));

                events(window).getSelectionModel().select(1);
                events(window).getSelectionModel().select(0);
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                assertEquals("2 registrations across 2 events with roles matching \"Speaker\".", feedback(window));

                tabs(window).getSelectionModel().select(0);
                enterCommand(window, "view-participant E1 P2");
                assertEquals("Events", selectedTab(window));
                assertEquals("E1", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals("Participants", heading(window, "searchHeading"));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of(WORKSHOP), events(window).getItems());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));
                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                assertEquals("1 registration across 1 event with roles matching \"Speaker\".", feedback(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void participantCommands_targetOutsideRoleSearchFilter_clearPanelUntilSearchResumes() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                for (String command : List.of("list-participants E2", "view-participant E2 P1")) {
                    enterCommand(window, "search-role Speaker /event E1");
                    enterCommand(window, command);
                    assertEquals("Events", selectedTab(window));
                    assertEquals(List.of(CONFERENCE), events(window).getItems());
                    assertNull(events(window).getSelectionModel().getSelectedItem());
                    assertTrue(participants(window).getItems().isEmpty());
                    assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));

                    enterCommand(window, "search-role Speaker");
                    assertEquals(List.of("E1/P1"), resultIds(window));
                    assertEquals("Matching \"Speaker\"", heading(window, "searchHeading"));
                }
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveRoleSearch_selectsArchivedMatchesAndPreservesSelectionAfterInvalidCommands() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker /include-archive");
                assertEquals("Events", selectedTab(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP, ARCHIVED), events(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("3 registrations across 3 events with roles matching \"Speaker\".", feedback(window));

                events(window).getSelectionModel().select(2);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", heading(window, "eventHeading"));
                assertEquals("Cara Lim", participants(window).getItems().getFirst().getPerson().getName().fullName);
                assertEquals(Set.of(new ParticipationRole("Speaker")),
                        participants(window).getItems().getFirst().getRoles());
                tabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("People", selectedTab(window));
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", heading(window, "eventHeading"));
                tabs(window).getSelectionModel().select(1);
                assertEquals("E3", events(window).getSelectionModel().getSelectedItem().getId());

                enterCommand(window, "search-role Speaker");
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveRoleSearch_emptyResultsAndArchivedScopes_showRequestedContext() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Missing /include-archive");
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("All events", heading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));
                enterCommand(window, "search-role Missing");
                assertEquals("All nonarchived events", heading(window, "eventHeading"));

                enterCommand(window, "search-role Speaker /event E3 /include-archive");
                assertEquals(List.of(ARCHIVED), events(window).getItems());
                assertEquals(List.of("E3/P3"), resultIds(window));
                enterCommand(window, "search-role Missing /event E3 /include-archive");
                assertTrue(events(window).getItems().isEmpty());
                assertTrue(participants(window).getItems().isEmpty());
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertEquals("Archived (E3)", heading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", heading(window, "searchHeading"));

                enterCommand(window, "list-event /include-archive");
                assertEquals(List.of(CONFERENCE, WORKSHOP, ARCHIVED), events(window).getItems());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("Participants", heading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, heading(window, "searchHeading"));
                enterCommand(window, "list-event");
                assertEquals(List.of(CONFERENCE, WORKSHOP), events(window).getItems());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void archiveRoleSearch_roleAndArchiveChanges_retainSelectionAndRefreshEmptyScope() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = createModel();
            MainWindow window = createWindow(model);
            try {
                enterCommand(window, "search-role Speaker /include-archive");
                events(window).getSelectionModel().select(1);
                enterCommand(window, "archive-event E2");
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("E2", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Workshop (E2)", heading(window, "eventHeading"));
                enterCommand(window, "unarchive-event E2");
                assertEquals("E2", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Workshop (E2)", heading(window, "eventHeading"));

                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals(List.of("E1/P1"), resultIds(window));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", heading(window, "eventHeading"));
                enterCommand(window, "edit-participant E3 P3 /role Guest");
                assertNull(events(window).getSelectionModel().getSelectedItem());
                assertTrue(participants(window).getItems().isEmpty());
                assertEquals("All events", heading(window, "eventHeading"));

                model.editParticipation(new EventId("E3"), new PersonId("P3"),
                        Set.of(new ParticipationRole("Speaker")), null);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("E3", events(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Archived (E3)", heading(window, "eventHeading"));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    private MainWindow createWindow() {
        return createWindow(createModel());
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

    private ModelManager createModel() {
        ModelManager model = new ModelManager();
        for (Event event : List.of(CONFERENCE, WORKSHOP, ARCHIVED)) {
            model.addEvent(event);
        }
        Person alice = new PersonBuilder().withId("P1").withName("Alice Tan").build();
        Person bob = new PersonBuilder().withId("P2").withName("Bob Lee").build();
        Person cara = new PersonBuilder().withId("P3").withName("Cara Lim").build();
        for (Person person : List.of(alice, bob, cara)) {
            model.addPerson(person);
        }
        model.addParticipation(new EventId("E1"), alice.getId(),
                Set.of(new ParticipationRole("Speaker"), new ParticipationRole("Host")), Set.of());
        model.addParticipation(new EventId("E1"), bob.getId(), Set.of(new ParticipationRole("Guest")), Set.of());
        model.addParticipation(new EventId("E2"), alice.getId(), Set.of(new ParticipationRole("Speaker")), Set.of());
        model.addParticipation(new EventId("E3"), cara.getId(), Set.of(new ParticipationRole("Speaker")), Set.of());
        return model;
    }

    private Person addWorkshopSpeaker(ModelManager model) {
        Person dana = new PersonBuilder().withId("P4").withName("Dana Lim").build();
        model.addPerson(dana);
        model.addParticipation(new EventId("E2"), dana.getId(), Set.of(new ParticipationRole("Speaker")), Set.of());
        return dana;
    }

    private void enterCommand(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private TabPane tabs(MainWindow window) {
        return (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
    }

    private String selectedTab(MainWindow window) {
        return tabs(window).getSelectionModel().getSelectedItem().getText();
    }

    @SuppressWarnings("unchecked")
    private ListView<Event> events(MainWindow window) {
        return (ListView<Event>) tabs(window).getTabs().get(1).getContent().lookup("#eventListView");
    }

    @SuppressWarnings("unchecked")
    private ListView<Participation> participants(MainWindow window) {
        return (ListView<Participation>) window.getParticipantListPanel().getRoot().lookup("#participantListView");
    }

    private List<String> resultIds(MainWindow window) {
        return participants(window).getItems().stream()
                .map(participation -> participation.getEvent().getId() + "/" + participation.getPerson().getId())
                .toList();
    }

    private String heading(MainWindow window, String id) {
        return ((Label) window.getParticipantListPanel().getRoot().lookup("#" + id)).getText();
    }

    private String feedback(MainWindow window) {
        return ((TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay")).getText();
    }
}
