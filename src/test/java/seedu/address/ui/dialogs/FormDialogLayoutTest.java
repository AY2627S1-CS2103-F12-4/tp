package seedu.address.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.UiButtonTestSupport.click;
import static seedu.address.testutil.UiButtonTestSupport.fields;
import static seedu.address.testutil.UiButtonTestSupport.interact;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.UiButtonTestSupport.Fixture;

public class FormDialogLayoutTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    public void showAndWait_longEventName_keepsActionsOnScreenAndPreservesName() throws Exception {
        runOnFxThread(() -> {
            try (Fixture fixture = new Fixture(temporaryFolder)) {
                String name = "A".repeat(500);
                fixture.logic.execute("add-event " + name + " /cap 2 /desc description");
                Event event = fixture.model.getEvents().getLast();
                Rectangle2D screen = showOwnerAtScreenEdge(fixture.owner);

                interact(() -> new EditEventDialog(fixture.owner, event).showAndWait(fixture.logic::execute), pane -> {
                    assertLayoutWithinScreen(pane, screen);
                    ScrollPane heading = (ScrollPane) pane.getHeader().lookup(".scroll-pane");
                    assertEquals(name, fields(pane).getFirst().getText());
                    assertTrue(heading.getViewportBounds().getHeight() >= 100,
                            "The wrapped heading must have room to display multiple lines: "
                                    + heading.getViewportBounds().getHeight());
                    fields(pane).get(1).setText("3");
                    click(pane, ButtonData.OK_DONE);
                });

                assertEquals(name, fixture.model.getEvents().getLast().getName());
                assertEquals(3, fixture.model.getEvents().getLast().getCapacity());
                fixture.assertPersisted();
            }
        });
    }

    @Test
    public void showAndWait_longNames_otherFormHeadersKeepActionsOnScreen() throws Exception {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            try {
                Rectangle2D screen = showOwnerAtScreenEdge(owner);
                String name = "A".repeat(500);
                Person person = new PersonBuilder().withName(name).build();
                Event event = new Event(new EventId("E1"), name, 2, "description");
                Participation participation = new Participation(event, person);
                Runnable addParticipant = () -> new AddParticipantDialog(owner, "E1", name).showAndWait(command -> {});
                Runnable editPerson = () -> new EditPersonDialog(owner, person, 1).showAndWait(command -> {});
                Runnable editParticipant = () -> new EditParticipantDialog(owner, participation)
                        .showAndWait(command -> {});
                List<Runnable> forms = List.of(addParticipant, editPerson, editParticipant);
                for (Runnable open : forms) {
                    interact(open, pane -> {
                        assertLayoutWithinScreen(pane, screen);
                        click(pane, ButtonData.CANCEL_CLOSE);
                    });
                }
            } finally {
                owner.close();
            }
        });
    }

    @Test
    public void showAndWait_veryLongEventName_scrollsWrappedHeadingWithoutHidingActions() throws Exception {
        runOnFxThread(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane()));
            try {
                Rectangle2D screen = showOwnerAtScreenEdge(owner);
                String name = "Long event name ".repeat(500).trim();
                Event event = new Event(new EventId("E1"), name, 2, "description");
                interact(() -> new EditEventDialog(owner, event).showAndWait(command -> {}), pane -> {
                    assertLayoutWithinScreen(pane, screen);
                    ScrollPane scroll = (ScrollPane) pane.getHeader().lookup(".scroll-pane");
                    Label header = (Label) scroll.getContent();
                    assertEquals("Edit " + name + ".", header.getText());
                    assertTrue(header.getHeight() > scroll.getViewportBounds().getHeight(),
                            "An exceptionally long heading must be scrollable");
                    scroll.setVvalue(scroll.getVmax());
                    assertEquals(name, fields(pane).getFirst().getText());
                    click(pane, ButtonData.CANCEL_CLOSE);
                });
            } finally {
                owner.close();
            }
        });
    }

    /** Positions the owner near a usable screen edge to exercise dialog placement as well as sizing. */
    private Rectangle2D showOwnerAtScreenEdge(Stage owner) {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        owner.setWidth(300);
        owner.setHeight(200);
        owner.setX(screen.getMaxX() - 300);
        owner.setY(screen.getMaxY() - 200);
        owner.show();
        return screen;
    }

    /** Checks rendered window, field and button bounds rather than only preferred layout sizes. */
    private void assertLayoutWithinScreen(DialogPane pane, Rectangle2D screen) {
        awaitLayout(pane.getScene());
        ScrollPane scroll = (ScrollPane) pane.getHeader().lookup(".scroll-pane");
        Label header = (Label) scroll.getContent();
        assertTrue(header.isWrapText());
        assertTrue(header.getHeight() > 2 * header.getFont().getSize(), "Long headings must wrap onto multiple lines");
        Window window = pane.getScene().getWindow();
        assertTrue(window.getX() >= screen.getMinX() - 1, "Dialog extends past the left screen edge");
        assertTrue(window.getY() >= screen.getMinY() - 1, "Dialog extends past the top screen edge");
        assertTrue(window.getX() + window.getWidth() <= screen.getMaxX() + 1,
                "Dialog extends past the right screen edge: x=" + window.getX()
                        + ", width=" + window.getWidth() + ", screen=" + screen);
        assertTrue(window.getY() + window.getHeight() <= screen.getMaxY() + 1,
                "Dialog extends past the bottom screen edge");
        for (Node node : fields(pane)) {
            assertNodeWithinScreen(node, screen);
        }
        pane.getButtonTypes().forEach(type -> assertNodeWithinScreen(pane.lookupButton(type), screen));
    }

    /** Verifies that a rendered control is fully visible in the usable screen area. */
    private void assertNodeWithinScreen(Node node, Rectangle2D screen) {
        Bounds bounds = node.localToScreen(node.getBoundsInLocal());
        assertTrue(node.isVisible());
        assertTrue(bounds.getMinX() >= screen.getMinX() - 1 && bounds.getMaxX() <= screen.getMaxX() + 1,
                "Control extends past a horizontal screen edge");
        assertTrue(bounds.getMinY() >= screen.getMinY() - 1 && bounds.getMaxY() <= screen.getMaxY() + 1,
                "Control extends past a vertical screen edge");
        Bounds sceneBounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(sceneBounds.getMinX() >= -1 && sceneBounds.getMaxX() <= node.getScene().getWidth() + 1,
                "Control is clipped by the dialog's horizontal bounds");
        assertTrue(sceneBounds.getMinY() >= -1 && sceneBounds.getMaxY() <= node.getScene().getHeight() + 1,
                node.getClass().getSimpleName() + " is clipped vertically: bounds=" + sceneBounds
                        + ", sceneHeight=" + node.getScene().getHeight());
    }

    /** Waits for layout pulses and deferred native bounds updates without blocking the FX thread. */
    private void awaitLayout(Scene scene) {
        Object loopKey = new Object();
        Runnable pulseListener = new Runnable() {
            private int remainingPulses = 10;

            @Override
            public void run() {
                if (--remainingPulses == 0) {
                    scene.removePostLayoutPulseListener(this);
                    Platform.runLater(() -> Platform.exitNestedEventLoop(loopKey, null));
                } else {
                    Platform.requestNextPulse();
                }
            }
        };
        scene.addPostLayoutPulseListener(pulseListener);
        try {
            Platform.requestNextPulse();
            Platform.enterNestedEventLoop(loopKey);
        } finally {
            scene.removePostLayoutPulseListener(pulseListener);
        }
    }
}
