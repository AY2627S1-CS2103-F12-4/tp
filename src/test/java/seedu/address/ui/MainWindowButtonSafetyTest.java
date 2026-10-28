package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.UiButtonTestSupport.assertError;
import static seedu.address.testutil.UiButtonTestSupport.click;
import static seedu.address.testutil.UiButtonTestSupport.fields;
import static seedu.address.testutil.UiButtonTestSupport.fill;
import static seedu.address.testutil.UiButtonTestSupport.interact;
import static seedu.address.testutil.UiButtonTestSupport.rowButton;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.io.TempDir;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.commands.AddParticipantCommand;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.testutil.UiButtonTestSupport.Fixture;
import seedu.address.testutil.UiButtonTestSupport.Snapshot;

/** Exercises actual toolbar/card buttons and their interaction with filters, identity and persistence. */
public class MainWindowButtonSafetyTest {
    @TempDir
    public Path temporaryFolder;

    @TestFactory
    public Stream<DynamicTest> deletion_cancelOrClose_neverChangesData() {
        return Arrays.stream(Target.values()).flatMap(target -> Stream.of(false, true).map(closeWindow ->
                test(target + (closeWindow ? " close confirmation" : " cancel confirmation"), (fixture, window) -> {
                    Button delete = target.button(window, "#deleteButton");
                    Snapshot before = fixture.snapshot();
                    interact(delete::fire, pane -> {
                        assertTrue(pane.getHeaderText().contains(target == Target.EVENT ? "Main" : "Alice Tan"));
                        if (closeWindow) {
                            Stage dialogStage = (Stage) pane.getScene().getWindow();
                            dialogStage.close();
                        } else {
                            click(pane, ButtonData.CANCEL_CLOSE);
                        }
                    });
                    fixture.assertUnchanged(before);
                })));
    }

    @TestFactory
    public Stream<DynamicTest> deletion_confirm_changesOnlyTheIntendedScope() {
        return Arrays.stream(Target.values()).map(target -> test(target + " confirm deletion", (fixture, window) -> {
            Set<Participation> otherRegistrations = fixture.model.getParticipationsForEvent(fixture.other);
            interact(target.button(window, "#deleteButton")::fire, pane -> click(pane, ButtonData.OK_DONE));
            switch (target) {
                case PERSON:
                    assertEquals(List.of(fixture.bob), fixture.model.getAddressBook().getPersonList());
                    assertEquals(List.of(fixture.main, fixture.other), fixture.model.getEvents());
                    assertTrue(fixture.model.getParticipations().getParticipations().isEmpty());
                    break;
                case EVENT:
                    assertEquals(List.of(fixture.other), fixture.model.getEvents());
                    assertEquals(List.of(fixture.alice, fixture.bob), fixture.model.getAddressBook().getPersonList());
                    assertEquals(otherRegistrations, fixture.model.getParticipations().getParticipations());
                    assertTrue(list(window, "#participantListView").getItems().isEmpty());
                    break;
                case PARTICIPANT:
                    assertEquals(List.of(fixture.main, fixture.other), fixture.model.getEvents());
                    assertEquals(List.of(fixture.alice, fixture.bob), fixture.model.getAddressBook().getPersonList());
                    assertTrue(fixture.model.getParticipationsForEvent(fixture.main).isEmpty());
                    assertEquals(otherRegistrations, fixture.model.getParticipationsForEvent(fixture.other));
                    assertTrue(list(window, "#participantListView").getItems().isEmpty());
                    break;
                default:
                    throw new AssertionError("Untested deletion " + target);
            }
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> participantRegistration_domainErrors_areVisibleWithoutMutations() {
        return Stream.of("duplicate", "missing person", "full event").map(state ->
                test("Add participant " + state, (fixture, window) -> {
                    if (state.equals("full event")) {
                        enterCommand(window, "edit-event E1 /cap 1");
                    }
                    String personId = state.equals("duplicate") ? "P1" : state.equals("missing person") ? "P999" : "P2";
                    String message = state.equals("duplicate")
                            ? String.format(AddParticipantCommand.MESSAGE_DUPLICATE_PARTICIPANT, "E1", "P1")
                            : state.equals("missing person")
                            ? String.format(AddParticipantCommand.MESSAGE_PERSON_NOT_FOUND, "P999")
                            : String.format(AddParticipantCommand.MESSAGE_EVENT_AT_CAPACITY, "E1");
                    Button add = eventButton(window, 0, "#addParticipantButton");
                    Snapshot before = fixture.snapshot();
                    interact(add::fire, pane -> {
                        fill(pane, List.of(personId, "Speaker", "Guest"));
                        click(pane, ButtonData.OK_DONE);
                        assertError(pane, message);
                        assertEquals(message, feedback(window));
                        fixture.assertUnchanged(before);
                    });
                    fixture.assertUnchanged(before);
                }));
    }

    @TestFactory
    public Stream<DynamicTest> personActions_filteredRowTargetsDisplayedPerson() {
        return Stream.of(false, true).map(delete -> test("Filtered person delete=" + delete, (fixture, window) -> {
            enterCommand(window, "find Bob");
            assertEquals(List.of(fixture.bob), list(window, "#personListView").getItems());
            Button action = rowButton(list(window, "#personListView"), 0,
                    delete ? "#deleteButton" : "#editButton");
            Set<Participation> registrations = fixture.model.getParticipations().getParticipations();
            interact(action::fire, pane -> {
                assertTrue(pane.getHeaderText().contains("Bob Lim"));
                if (!delete) {
                    assertEquals("Bob Lim", fields(pane).getFirst().getText());
                    fields(pane).getFirst().setText("Bob Filtered");
                }
                click(pane, ButtonData.OK_DONE);
            });
            assertEquals(fixture.alice, fixture.model.getAddressBook().getPersonList().getFirst());
            assertEquals(registrations, fixture.model.getParticipations().getParticipations());
            if (delete) {
                assertEquals(1, fixture.model.getAddressBook().getPersonList().size());
            } else {
                Person edited = fixture.model.getAddressBook().getPersonList().getLast();
                assertEquals(fixture.bob.getId(), edited.getId());
                assertEquals("Bob Filtered", edited.getName().fullName);
                assertEquals(fixture.bob.getPhone(), edited.getPhone());
                assertEquals(fixture.bob.getTags(), edited.getTags());
            }
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> eventActions_filteredRowUsesStableId() {
        return Stream.of(false, true).map(delete -> test("Filtered event delete=" + delete, (fixture, window) -> {
            enterCommand(window, "find-event Other");
            assertEquals(List.of(fixture.other), list(window, "#eventListView").getItems());
            Button action = eventButton(window, 0, delete ? "#deleteButton" : "#editButton");
            var mainRegistration = fixture.model.getParticipationsForEvent(fixture.main);
            interact(action::fire, pane -> {
                assertTrue(pane.getHeaderText().contains("Other"));
                if (!delete) {
                    fields(pane).get(2).setText("Updated other event");
                }
                click(pane, ButtonData.OK_DONE);
            });
            assertEquals(fixture.main, fixture.model.getEvents().getFirst());
            assertEquals(mainRegistration, fixture.model.getParticipationsForEvent(fixture.main));
            if (delete) {
                assertEquals(List.of(fixture.main), fixture.model.getEvents());
            } else {
                assertEquals("E2", fixture.model.getEvents().getLast().getId());
                assertEquals("Updated other event", fixture.model.getEvents().getLast().getDescription());
            }
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> toolbar_cancelForms_doesNotChangeTabOrData() {
        return Stream.of("#addPersonButton", "#addEventButton").map(selector -> test(selector, (fixture, window) -> {
            tabs(window).getSelectionModel().select(1);
            Snapshot before = fixture.snapshot();
            Button add = (Button) fixture.owner.getScene().lookup(selector);
            interact(add::fire, pane -> click(pane, ButtonData.CANCEL_CLOSE));
            assertEquals(1, tabs(window).getSelectionModel().getSelectedIndex());
            fixture.assertUnchanged(before);
        }));
    }

    @TestFactory
    public Stream<DynamicTest> archiveAndRestore_renderedButtonsPreserveIdentityAndRegistrations() {
        return Stream.of(0, 1).map(index -> test("Archive and restore row " + index, (fixture, window) -> {
            var registrations = fixture.model.getParticipations().getParticipations();
            String id = fixture.model.getEvents().get(index).getId();
            Button archive = eventButton(window, index, "#archiveButton");
            assertEquals("Archive", archive.getText());
            archive.fire();
            assertTrue(fixture.model.getEvents().get(index).isArchived());
            assertFalse(list(window, "#eventListView").getItems().contains(fixture.model.getEvents().get(index)));

            enterCommand(window, "list-event /include-archive");
            Button restore = eventButton(window, index, "#archiveButton");
            assertEquals("Unarchive", restore.getText());
            restore.fire();
            assertFalse(fixture.model.getEvents().get(index).isArchived());
            assertEquals(id, fixture.model.getEvents().get(index).getId());
            assertEquals(registrations, fixture.model.getParticipations().getParticipations());
            fixture.assertPersisted();
        }));
    }

    @TestFactory
    public Stream<DynamicTest> reusedPersonRow_afterPreviousDeletion_hasCurrentTarget() {
        List<String> actions = List.of("#editButton", "#deleteButton");
        return actions.stream().map(selector -> test("Reused " + selector, (fixture, window) -> {
            interact(rowButton(list(window, "#personListView"), 0, "#deleteButton")::fire,
                    pane -> click(pane, ButtonData.OK_DONE));
            assertEquals(List.of(fixture.bob), list(window, "#personListView").getItems());
            interact(rowButton(list(window, "#personListView"), 0, selector)::fire, pane -> {
                assertTrue(pane.getHeaderText().contains("Bob Lim"));
                if (selector.equals("#editButton")) {
                    fields(pane).getFirst().setText("Bob Remaining");
                }
                click(pane, ButtonData.OK_DONE);
            });
            if (selector.equals("#editButton")) {
                assertEquals(fixture.bob.getId(),
                        fixture.model.getAddressBook().getPersonList().getFirst().getId());
                assertEquals("Bob Remaining", fixture.model.getAddressBook().getPersonList().getFirst()
                        .getName().fullName);
            } else {
                assertTrue(fixture.model.getAddressBook().getPersonList().isEmpty());
            }
            fixture.assertPersisted();
        }));
    }

    private DynamicTest test(String name, WindowScenario scenario) {
        return DynamicTest.dynamicTest(name, () -> runOnFxThread(() -> {
            try (Fixture fixture = new Fixture(Files.createTempDirectory(temporaryFolder, "buttons-"))) {
                MainWindow window = new MainWindow(fixture.owner, fixture.logic,
                        fixture.storage.getAddressBookFilePath());
                window.fillInnerParts();
                window.show();
                fixture.owner.getScene().getRoot().applyCss();
                fixture.owner.getScene().getRoot().layout();
                scenario.run(fixture, window);
            }
        }));
    }

    private static ListView<?> list(MainWindow window, String selector) {
        window.getPrimaryStage().getScene().getRoot().applyCss();
        window.getPrimaryStage().getScene().getRoot().layout();
        return (ListView<?>) window.getPrimaryStage().getScene().lookup(selector);
    }

    private static TabPane tabs(MainWindow window) {
        return (TabPane) window.getPrimaryStage().getScene().lookup("#listTabs");
    }

    private static Button eventButton(MainWindow window, int index, String selector) {
        tabs(window).getSelectionModel().select(1);
        return rowButton(list(window, "#eventListView"), index, selector);
    }

    private static void enterCommand(MainWindow window, String command) {
        TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        input.setText(command);
        input.fireEvent(new ActionEvent());
    }

    private static String feedback(MainWindow window) {
        return ((TextArea) window.getPrimaryStage().getScene().lookup("#resultDisplay")).getText();
    }

    @FunctionalInterface
    private interface WindowScenario {
        void run(Fixture fixture, MainWindow window) throws Exception;
    }

    private enum Target {
        PERSON, EVENT, PARTICIPANT;

        Button button(MainWindow window, String selector) {
            if (this == PERSON) {
                tabs(window).getSelectionModel().select(0);
                return rowButton(list(window, "#personListView"), 0, selector);
            }
            tabs(window).getSelectionModel().select(1);
            if (this == EVENT) {
                return rowButton(list(window, "#eventListView"), 0, selector);
            }
            list(window, "#eventListView").getSelectionModel().select(0);
            return rowButton(list(window, "#participantListView"), 0, selector);
        }
    }
}
