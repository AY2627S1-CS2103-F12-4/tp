package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.SearchRoleCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
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
                    List<Event> matchingEvents = List.copyOf(getEvents(window).getItems());
                    Event selected = matchingEvents.getLast();
                    getEvents(window).getSelectionModel().select(selected);
                    List<String> registrations = resultIds(window);
                    String eventHeading = getHeading(window, "eventHeading");
                    String searchHeading = getHeading(window, "searchHeading");
                    int eventCount = model.getEvents().size();

                    enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                    assertEquals("Event added successfully.", getFeedback(window));
                    assertEquals(eventCount + 1, model.getEvents().size());
                    assertEquals("Events", getSelectedTabName(window));
                    assertEquals(matchingEvents, getEvents(window).getItems());
                    assertEquals(selected, getEvents(window).getSelectionModel().getSelectedItem());
                    assertEquals(registrations, resultIds(window));
                    assertEquals(eventHeading, getHeading(window, "eventHeading"));
                    assertEquals(searchHeading, getHeading(window, "searchHeading"));
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
                    assertTrue(getEvents(window).getItems().isEmpty());
                    String eventHeading = getHeading(window, "eventHeading");
                    int eventCount = model.getEvents().size();

                    enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                    assertEquals("Event added successfully.", getFeedback(window));
                    assertEquals(eventCount + 1, model.getEvents().size());
                    assertEquals("Events", getSelectedTabName(window));
                    assertTrue(getEvents(window).getItems().isEmpty());
                    assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                    assertTrue(getParticipants(window).getItems().isEmpty());
                    assertEquals(eventHeading, getHeading(window, "eventHeading"));
                    assertEquals("Matching \"Missing\"", getHeading(window, "searchHeading"));
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
                assertEquals(List.of(matchingEvent), getEvents(window).getItems());

                enterCommand(window, "add-event Empty /cap 10 /desc No participants");

                assertEquals("Event added successfully.", getFeedback(window));
                assertEquals(List.of("E1", "E2", "E3"), model.getEvents().stream().map(Event::getId).toList());
                assertEquals(List.of(matchingEvent), getEvents(window).getItems());
                assertEquals(matchingEvent, getEvents(window).getSelectionModel().getSelectedItem());
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Other (E2)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                assertEquals("Events", getSelectedTabName(window));
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
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of("People", "Events"), getTabs(window).getTabs().stream()
                        .map(tab -> tab.getText()).toList());
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"sPeAkEr\"", getHeading(window, "searchHeading"));
                assertEquals("2 registrations across 2 events with roles matching \"sPeAkEr\".", getFeedback(window));
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(Set.of(new ParticipationRole("Speaker"), new ParticipationRole("Host")),
                        getParticipants(window).getItems().getFirst().getRoles());
                getEvents(window).getSelectionModel().select(1);
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"sPeAkEr\"", getHeading(window, "searchHeading"));
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
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of(CONFERENCE), getEvents(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", getHeading(window, "eventHeading"));
                assertEquals("1 registration across 1 event with roles matching \"Speaker\".", getFeedback(window));

                enterCommand(window, "archive-event E1");
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(CONFERENCE.withArchived(true).getName() + " (E1)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E1");
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());

                enterCommand(window, "search-role Missing /event E1");
                assertEquals("Events", getSelectedTabName(window));
                assertTrue(getEvents(window).getItems().isEmpty());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Conference (E1)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", getHeading(window, "searchHeading"));
                assertEquals("No registrations with roles matching \"Missing\" in nonarchived events.",
                        getFeedback(window));
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());

                enterCommand(window, "search-role Missing");
                assertEquals("All nonarchived events", getHeading(window, "eventHeading"));
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                enterCommand(window, "search-role Speaker /event E3");
                assertEquals(ARCHIVED.getName() + " (E3)", getHeading(window, "eventHeading"));
                assertTrue(getEvents(window).getItems().isEmpty());
                assertTrue(getParticipants(window).getItems().isEmpty());
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
                getEvents(window).getSelectionModel().select(1);
                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("Event E99 does not exist.", getFeedback(window));
                assertEquals("People", getSelectedTabName(window));
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));

                enterCommand(window, "search-role Speaker /event");
                assertTrue(getFeedback(window).contains(SearchRoleCommand.MESSAGE_USAGE));
                assertEquals("People", getSelectedTabName(window));
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
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
                getTabs(window).getSelectionModel().select(0);
                getTabs(window).getSelectionModel().select(1);
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));

                enterCommand(window, "list-event");
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Participants", getHeading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));
                assertEquals("Listing events...", getFeedback(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertTrue(getParticipants(window).getItems().isEmpty());

                enterCommand(window, "search-role Speaker");
                assertEquals(2, getParticipants(window).getItems().size());
                enterCommand(window, "find-event Workshop");
                assertEquals(List.of(WORKSHOP), getEvents(window).getItems());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));
                assertEquals("Showing event search results for Workshop...", getFeedback(window));

                enterCommand(window, "find-event Archived /include-archive");
                assertEquals(List.of(ARCHIVED), getEvents(window).getItems());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Showing event search results for Archived...", getFeedback(window));

                enterCommand(window, "find-event Missing /include-archive");
                assertTrue(getEvents(window).getItems().isEmpty());
                assertEquals("No matching events found.", getFeedback(window));

                enterCommand(window, "find-event Archived");
                assertTrue(getEvents(window).getItems().isEmpty());
                assertEquals("No matching events found.", getFeedback(window));
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
                assertEquals(List.of(WORKSHOP), getEvents(window).getItems());
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));
                enterCommand(window, "archive-event E2");
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertTrue(getEvents(window).getItems().isEmpty());
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E2");
                assertEquals(List.of("E2/P1"), resultIds(window));

                enterCommand(window, "search-participant Alice /event E1");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals("Conference (E1)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Alice\"", getHeading(window, "searchHeading"));
                assertEquals("1 matching registration across 1 event for \"Alice\".", getFeedback(window));
                enterCommand(window, "search-role Speaker /event E1");
                assertTrue(getParticipants(window).getItems().isEmpty());
                enterCommand(window, "edit-participant E1 P1 /role Speaker");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Conference (E1)", getHeading(window, "eventHeading"));
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
                getEvents(window).getSelectionModel().select(1);
                assertEquals(List.of("E2/P1", "E2/P4"), resultIds(window));
                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                enterCommand(window, "edit-event E2 Renamed Workshop");
                assertEquals("Renamed Workshop (E2)", getHeading(window, "eventHeading"));
                assertEquals(List.of("E2/P4"), resultIds(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertEquals(List.of("E2/P4"), resultIds(window));
                enterCommand(window, "del-participant E2 P4");
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of("E1/P2"), resultIds(window));
                enterCommand(window, "del-participant E1 P2");
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("All nonarchived events", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
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
                getEvents(window).getSelectionModel().select(1);
                model.editParticipation(new EventId("E2"), new PersonId("P1"),
                        Set.of(new ParticipationRole("Guest")), null);
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                model.setPerson(dana, new PersonBuilder(dana).withName("Dana Updated").build());
                assertEquals("Dana Updated",
                        getParticipants(window).getItems().getFirst().getPerson().getName().fullName);
                model.markPresent(new EventId("E2"), dana.getId());
                assertTrue(getParticipants(window).getItems().getFirst().isPresent());
                assertEquals(List.of("E2/P4"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
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
                getEvents(window).getSelectionModel().select(1);
                enterCommand(window, "del-event E2");
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Speaker", model.getParticipantSearchKeyword().orElseThrow());
                enterCommand(window, "search-role Speaker /event E1");
                enterCommand(window, "del-event E1");
                assertTrue(model.getParticipantSearchKeyword().isEmpty());
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Participants", getHeading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));
                enterCommand(window, "unarchive-event E3");
                assertTrue(getParticipants(window).getItems().isEmpty());
                getEvents(window).getSelectionModel().select(0);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Participants", getHeading(window, "searchHeading"));
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
                assertEquals("1 matching registration across 1 event for \"Speaker\".", getFeedback(window));
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("Event E99 does not exist.", getFeedback(window));
                enterCommand(window, "edit-participant E1 P1 /role Speaker");
                assertEquals(List.of("E1/P2"), resultIds(window));

                enterCommand(window, "search-role Speaker");
                assertEquals("2 registrations across 2 events with roles matching \"Speaker\".", getFeedback(window));
                assertEquals(List.of("E1/P1"), resultIds(window));
                enterCommand(window, "search-participant Speaker /event E99");
                assertEquals("Event E99 does not exist.", getFeedback(window));
                enterCommand(window, "edit-participant E1 P2 /role Speaker");
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
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
                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "list-participants E1");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals("Participants", getHeading(window, "searchHeading"));

                getEvents(window).getSelectionModel().select(1);
                getEvents(window).getSelectionModel().select(0);
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                assertEquals("2 registrations across 2 events with roles matching \"Speaker\".", getFeedback(window));

                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "view-participant E1 P2");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals("E1", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E1/P1", "E1/P2"), resultIds(window));
                assertEquals("Participants", getHeading(window, "searchHeading"));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of(WORKSHOP), getEvents(window).getItems());
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));
                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                assertEquals("1 registration across 1 event with roles matching \"Speaker\".", getFeedback(window));
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void listParticipants_targetOutsideRoleSearchFilter_clearsSearchAndDisplaysTarget() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker /event E1");
                enterCommand(window, "list-participants E2");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));
                assertEquals("Participants", getHeading(window, "searchHeading"));
                Button clearSearchButton = (Button) window.getPrimaryStage().getScene().lookup("#clearSearchButton");
                assertFalse(clearSearchButton.isVisible());

                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals(Set.of(new ParticipationRole("Guest")),
                        getParticipants(window).getItems().getFirst().getRoles());
                assertFalse(clearSearchButton.isVisible());

                enterCommand(window, "search-role Speaker");
                assertEquals(List.of(CONFERENCE), getEvents(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
                assertTrue(clearSearchButton.isVisible());
            } finally {
                window.getPrimaryStage().close();
            }
        });
    }

    @Test
    public void viewParticipant_targetOutsideRoleSearchFilter_clearsPanelUntilSearchResumes() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = createWindow();
            try {
                enterCommand(window, "search-role Speaker /event E1");
                enterCommand(window, "view-participant E2 P1");
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of(CONFERENCE), getEvents(window).getItems());
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));

                enterCommand(window, "search-role Speaker");
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("Matching \"Speaker\"", getHeading(window, "searchHeading"));
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
                assertEquals("Events", getSelectedTabName(window));
                assertEquals(List.of(CONFERENCE, WORKSHOP, ARCHIVED), getEvents(window).getItems());
                assertEquals(List.of("E1/P1"), resultIds(window));
                assertEquals("3 registrations across 3 events with roles matching \"Speaker\".", getFeedback(window));

                getEvents(window).getSelectionModel().select(2);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
                assertEquals("Cara Lim", getParticipants(window).getItems().getFirst().getPerson().getName().fullName);
                assertEquals(Set.of(new ParticipationRole("Speaker")),
                        getParticipants(window).getItems().getFirst().getRoles());
                getTabs(window).getSelectionModel().select(0);
                enterCommand(window, "search-role Speaker /event E99");
                assertEquals("People", getSelectedTabName(window));
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
                getTabs(window).getSelectionModel().select(1);
                assertEquals("E3", getEvents(window).getSelectionModel().getSelectedItem().getId());

                enterCommand(window, "search-role Speaker");
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
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
                assertTrue(getEvents(window).getItems().isEmpty());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertEquals("All events", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", getHeading(window, "searchHeading"));
                enterCommand(window, "search-role Missing");
                assertEquals("All nonarchived events", getHeading(window, "eventHeading"));

                enterCommand(window, "search-role Speaker /event E3 /include-archive");
                assertEquals(List.of(ARCHIVED), getEvents(window).getItems());
                assertEquals(List.of("E3/P3"), resultIds(window));
                enterCommand(window, "search-role Missing /event E3 /include-archive");
                assertTrue(getEvents(window).getItems().isEmpty());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
                assertEquals("Matching \"Missing\"", getHeading(window, "searchHeading"));

                enterCommand(window, "list-event /include-archive");
                assertEquals(List.of(CONFERENCE, WORKSHOP, ARCHIVED), getEvents(window).getItems());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("Participants", getHeading(window, "eventHeading"));
                assertEquals(SELECT_EVENT_PROMPT, getHeading(window, "searchHeading"));
                enterCommand(window, "list-event");
                assertEquals(List.of(CONFERENCE, WORKSHOP), getEvents(window).getItems());
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
                getEvents(window).getSelectionModel().select(1);
                enterCommand(window, "archive-event E2");
                assertEquals(List.of("E2/P1"), resultIds(window));
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));
                enterCommand(window, "unarchive-event E2");
                assertEquals("E2", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Workshop (E2)", getHeading(window, "eventHeading"));

                enterCommand(window, "edit-participant E2 P1 /role Guest");
                assertEquals(List.of("E1/P1"), resultIds(window));
                enterCommand(window, "edit-participant E1 P1 /role Guest");
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
                enterCommand(window, "edit-participant E3 P3 /role Guest");
                assertNull(getEvents(window).getSelectionModel().getSelectedItem());
                assertTrue(getParticipants(window).getItems().isEmpty());
                assertEquals("All events", getHeading(window, "eventHeading"));

                model.editParticipation(new EventId("E3"), new PersonId("P3"),
                        Set.of(new ParticipationRole("Speaker")), null);
                assertEquals(List.of("E3/P3"), resultIds(window));
                assertEquals("E3", getEvents(window).getSelectionModel().getSelectedItem().getId());
                assertEquals("Archived (E3)", getHeading(window, "eventHeading"));
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
        prepareEventTab(window);
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

    private List<String> resultIds(MainWindow window) {
        return getParticipants(window).getItems().stream()
                .map(participation -> participation.getEvent().getId() + "/" + participation.getPerson().getId())
                .toList();
    }
}
