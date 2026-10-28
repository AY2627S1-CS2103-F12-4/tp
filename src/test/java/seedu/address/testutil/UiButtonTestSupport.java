package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
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

/** Shared fixtures and deterministic modal interaction for button integration tests. */
public final class UiButtonTestSupport {
    private UiButtonTestSupport() {}

    /** Opens one modal, propagates callback failures, and closes remaining dialogs even after a failed assertion. */
    public static void interact(Runnable openDialog, DialogAction action) throws Exception {
        Set<Window> existingWindows = Set.copyOf(Window.getWindows());
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean active = new AtomicBoolean(true);
        AtomicBoolean interacted = new AtomicBoolean();
        Platform.runLater(() -> {
            if (!active.get()) {
                return;
            }
            try {
                DialogPane pane = Window.getWindows().stream().filter(Window::isShowing)
                        .filter(window -> !existingWindows.contains(window))
                        .map(Window::getScene).filter(scene -> scene != null).map(Scene::getRoot)
                        .filter(DialogPane.class::isInstance).map(DialogPane.class::cast)
                        .findFirst().orElseThrow(() -> new AssertionError("The button did not open a dialog"));
                interacted.set(true);
                action.run(pane);
            } catch (Throwable throwable) {
                failure.set(throwable);
            } finally {
                new ArrayList<>(Window.getWindows()).stream()
                        .filter(window -> !existingWindows.contains(window)).forEach(Window::hide);
            }
        });
        try {
            openDialog.run();
        } finally {
            active.set(false);
        }
        if (failure.get() != null) {
            throw new AssertionError("Dialog interaction failed", failure.get());
        }
        assertTrue(interacted.get(), "The button must open a dialog synchronously");
    }

    /** Returns fields in visual row order, without depending on prompts or private controller fields. */
    public static List<TextField> fields(DialogPane pane) {
        GridPane form = (GridPane) pane.lookup(".dialog-form");
        assertNotNull(form);
        return form.getChildren().stream().filter(TextField.class::isInstance).map(TextField.class::cast)
                .sorted(java.util.Comparator.comparingInt(field -> GridPane.getRowIndex(field)))
                .toList();
    }

    /** Fills every field; a changed form shape fails the test instead of silently leaving a field unset. */
    public static void fill(DialogPane pane, List<String> values) {
        List<TextField> fields = fields(pane);
        assertEquals(values.size(), fields.size());
        for (int i = 0; i < values.size(); i++) {
            fields.get(i).setText(values.get(i));
        }
    }

    /** Fires the dialog action by its semantic role. */
    public static void click(DialogPane pane, ButtonData role) {
        Button button = (Button) pane.getButtonTypes().stream().filter(type -> type.getButtonData() == role)
                .map(pane::lookupButton).findFirst().orElseThrow();
        button.fire();
    }

    /** Verifies rejection is visible and the dialog remains open for correction. */
    public static void assertError(DialogPane pane, String expectedMessage) {
        assertTrue(isShowing(pane), "Invalid submissions must keep the form open");
        Label error = (Label) pane.lookup(".validation-error-label");
        assertNotNull(error);
        assertTrue(error.isVisible());
        assertTrue(error.isManaged());
        assertFalse(error.getText().isBlank());
        if (expectedMessage != null) {
            assertEquals(expectedMessage, error.getText());
        }
    }

    /** JavaFX may detach a closed dialog pane from its window immediately after a button is fired. */
    public static boolean isShowing(DialogPane pane) {
        return pane.getScene() != null && pane.getScene().getWindow() != null
                && pane.getScene().getWindow().isShowing();
    }

    /** Returns a button from a rendered row, including rows reused after filtering or deletion. */
    public static Button rowButton(ListView<?> list, int index, String selector) {
        list.scrollTo(index);
        list.applyCss();
        list.layout();
        ListCell<?> cell = list.lookupAll(".list-cell").stream().filter(ListCell.class::isInstance)
                .map(node -> (ListCell<?>) node).filter(row -> row.getIndex() == index && !row.isEmpty())
                .findFirst().orElseThrow(() -> new AssertionError("No rendered row at index " + index));
        Button button = (Button) cell.getGraphic().lookup(selector);
        assertNotNull(button);
        return button;
    }

    /** A modal assertion that can call the real logic and storage APIs. */
    @FunctionalInterface
    public interface DialogAction {
        void run(DialogPane pane) throws Exception;
    }

    /** Two people and two events, with one person registered in both events to detect unintended side effects. */
    public static final class Fixture implements AutoCloseable {
        public final ModelManager model = new ModelManager();
        public final Person alice = new PersonBuilder().withId("P1").withName("Alice Tan")
                .withPhone("91234567").withEmail("alice@example.com").withAddress("1 Main Street")
                .withTags("friend").build();
        public final Person bob = new PersonBuilder().withId("P2").withName("Bob Lim")
                .withPhone("92345678").withEmail("bob@example.com").withAddress("2 Main Street")
                .withTags("colleague").build();
        public final Event main = new Event(new EventId("E1"), "Main", 2, "Original");
        public final Event other = new Event(new EventId("E2"), "Other", 2, "Other event");
        public final Stage owner = new Stage();
        public final StorageManager storage;
        public final LogicManager logic;

        /** Seeds real files before constructing logic, just as a loaded application does. */
        public Fixture(Path directory) throws Exception {
            owner.setScene(new Scene(new StackPane()));
            model.addPerson(alice);
            model.addPerson(bob);
            model.addEvent(main);
            model.addEvent(other);
            model.addParticipation(new EventId("E1"), alice.getId(),
                    Set.of(new ParticipationRole("Host")), Set.of(new Tag("VIP")));
            model.setPresent(main, alice, true);
            model.addParticipation(new EventId("E2"), alice.getId(),
                    Set.of(new ParticipationRole("Speaker")), Set.of(new Tag("Guest")));
            storage = new StorageManager(new JsonAddressBookStorage(directory.resolve("people.json")),
                    new JsonUserPrefsStorage(directory.resolve("prefs.json")),
                    new JsonParticipationStorage(directory.resolve("participations.json")));
            storage.saveAddressBook(model.getAddressBook());
            storage.saveEvents(model.getEvents());
            storage.saveParticipations(model.getParticipations());
            logic = new LogicManager(model, storage);
        }

        /** Captures both domain data and exact file contents to detect writes or partial updates on rejection. */
        public Snapshot snapshot() throws Exception {
            return new Snapshot(new AddressBook(model.getAddressBook()), List.copyOf(model.getEvents()),
                    model.getParticipations().getParticipations(), readFiles());
        }

        /** Checks all data, including unrelated entities and their persisted records. */
        public void assertUnchanged(Snapshot before) throws Exception {
            assertEquals(before, snapshot());
        }

        /** Checks a successful button action survives a complete storage reload. */
        public void assertPersisted() throws Exception {
            AddressBook people = new AddressBook(storage.readAddressBook().orElseThrow());
            var events = storage.readEvents().orElseThrow();
            assertEquals(model.getAddressBook(), people);
            assertEquals(model.getAddressBook().getPersonList().stream().map(Person::getId).toList(),
                    people.getPersonList().stream().map(Person::getId).toList());
            assertEquals(model.getEvents(), events.getEvents());
            assertEquals(model.getParticipations().getParticipations(),
                    storage.readParticipations(events, people).orElseThrow().getParticipations());
        }

        private List<String> readFiles() throws Exception {
            return List.of(Files.readString(storage.getAddressBookFilePath()),
                    Files.readString(storage.getEventFilePath()), Files.readString(storage.getParticipationFilePath()));
        }

        @Override
        public void close() {
            owner.close();
        }
    }

    /** Immutable expected data captured before a cancelled or invalid action. */
    public record Snapshot(AddressBook people, List<Event> events, Set<Participation> participations,
                           List<String> files) {}
}
