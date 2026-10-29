package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;

public class EntryCardActionButtonTest {

    private static final double NARROW_VIEWPORT_WIDTH = 220;
    private static final double VIEWPORT_HEIGHT = 400;
    private static final double BOUNDS_TOLERANCE = 0.5;

    @Test
    public void eventCard_archiveState_displaysSeparateRedLabelAfterName() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(
                    new EventId("E1"),
                    "Training <archived>",
                    20,
                    "Learn Java");

            EventCard archived = new EventCard(event.withArchived(true), 1, () -> { }, () -> { });

            Label archivedName =
                    (Label) archived.getRoot().lookup("#name");
            assertEquals("Training <archived>", archivedName.getText());

            Label status =
                    (Label) archived.getRoot().lookup("#archivedStatus");
            assertEquals("Archived", status.getText());
            assertTrue(status.isVisible());
            assertTrue(status.isManaged());

            HBox heading = (HBox) archivedName.getParent();
            int nameIndex = heading.getChildren().indexOf(archivedName);
            assertSame(status, heading.getChildren().get(nameIndex + 1));

            StackPane root = new StackPane(archived.getRoot());
            root.getStylesheets().add("view/DarkTheme.css");
            new Scene(root);
            root.applyCss();

            assertEquals(
                    Color.web("#a52d37"),
                    status.getBackground().getFills().getFirst().getFill());
            assertEquals(Color.WHITE, status.getTextFill());

            Button archivedButton =
                    (Button) archived.getRoot().lookup("#archiveButton");
            assertEquals("Unarchive", archivedButton.getText());

            EventCard active = new EventCard(event, 1, () -> { }, () -> { });

            Label activeName =
                    (Label) active.getRoot().lookup("#name");
            assertEquals("Training <archived>", activeName.getText());

            Label activeStatus =
                    (Label) active.getRoot().lookup("#archivedStatus");
            assertFalse(activeStatus.isVisible());
            assertFalse(activeStatus.isManaged());

            Button activeButton =
                    (Button) active.getRoot().lookup("#archiveButton");
            assertEquals("Archive", activeButton.getText());

            assertEquals(
                    "Training <archived>",
                    event.withArchived(true).getName());
        });
    }

    @Test
    public void personCard_actionButtons_runConfiguredActionsInEditDeleteOrder()
            throws Exception {
        runOnFxThread(() -> {
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();

            PersonCard card = new PersonCard(
                    ALICE,
                    1,
                    editActions::incrementAndGet,
                    deleteActions::incrementAndGet);

            Button editButton =
                    (Button) card.getRoot().lookup("#editButton");
            Button deleteButton =
                    (Button) card.getRoot().lookup("#deleteButton");

            assertEquals("Edit", editButton.getText());
            assertButtonOrder(editButton, deleteButton);
            assertRightGap(card.getRoot(), deleteButton);

            editButton.fire();
            deleteButton.fire();

            assertEquals(1, editActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void participantCard_actionButtons_runConfiguredActionsInEditDeleteOrder()
            throws Exception {
        runOnFxThread(() -> {
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();

            Event event = new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

            ParticipantCard card = new ParticipantCard(
                    new Participation(event, ALICE),
                    editActions::incrementAndGet,
                    deleteActions::incrementAndGet);

            Button editButton =
                    (Button) card.getRoot().lookup("#editButton");
            Button deleteButton =
                    (Button) card.getRoot().lookup("#deleteButton");

            assertEquals("Edit participation", editButton.getText());
            assertButtonOrder(editButton, deleteButton);
            assertRightGap(card.getRoot(), deleteButton);

            editButton.fire();
            deleteButton.fire();

            assertEquals(1, editActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void eventCard_actionButtons_runConfiguredActionsInRequestedOrder()
            throws Exception {
        runOnFxThread(() -> {
            AtomicInteger addParticipantActions = new AtomicInteger();
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger archiveActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();

            Event event = new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

            EventCard card = new EventCard(
                    event,
                    1,
                    addParticipantActions::incrementAndGet,
                    editActions::incrementAndGet,
                    archiveActions::incrementAndGet,
                    deleteActions::incrementAndGet);

            Button addParticipantButton =
                    (Button) card.getRoot().lookup("#addParticipantButton");
            Button editButton =
                    (Button) card.getRoot().lookup("#editButton");
            Button archiveButton =
                    (Button) card.getRoot().lookup("#archiveButton");
            Button deleteButton =
                    (Button) card.getRoot().lookup("#deleteButton");

            assertEquals("Edit event", editButton.getText());
            assertButtonOrder(
                    addParticipantButton,
                    editButton,
                    archiveButton,
                    deleteButton);
            assertBrownBorder(card.getRoot(), archiveButton);
            assertRightGap(card.getRoot(), deleteButton);

            addParticipantButton.fire();
            editButton.fire();
            archiveButton.fire();
            deleteButton.fire();

            assertEquals(1, addParticipantActions.get());
            assertEquals(1, editActions.get());
            assertEquals(1, archiveActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void participantCard_narrowViewport_actionButtonsRemainVisible()
            throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

            ParticipantCard card = new ParticipantCard(
                    new Participation(event, ALICE));

            Button editButton =
                    (Button) card.getRoot().lookup("#editButton");
            Button deleteButton =
                    (Button) card.getRoot().lookup("#deleteButton");

            assertButtonsWithinViewport(
                    card.getRoot(),
                    editButton,
                    deleteButton);
        });
    }

    @Test
    public void eventCard_narrowViewport_actionButtonsRemainVisible()
            throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

            EventCard card = new EventCard(event, 1, () -> { }, () -> { });

            Button addParticipantButton =
                    (Button) card.getRoot().lookup("#addParticipantButton");
            Button editButton =
                    (Button) card.getRoot().lookup("#editButton");
            Button archiveButton =
                    (Button) card.getRoot().lookup("#archiveButton");
            Button deleteButton =
                    (Button) card.getRoot().lookup("#deleteButton");

            assertButtonsWithinViewport(
                    card.getRoot(),
                    addParticipantButton,
                    editButton,
                    archiveButton,
                    deleteButton);
        });
    }

    @Test
    public void eventCard_addParticipantButton_runsConfiguredAction()
            throws Exception {
        runOnFxThread(() -> {
            AtomicInteger addParticipantActions = new AtomicInteger();

            Event event = new Event(
                    new EventId("E1"),
                    "Workshop",
                    20,
                    "Learn Java");

            EventCard card = new EventCard(event, 1,
                    addParticipantActions::incrementAndGet, () -> { });

            Button addParticipantButton =
                    (Button) card.getRoot().lookup("#addParticipantButton");

            assertEquals(
                    "Add participant",
                    addParticipantButton.getText());

            addParticipantButton.fire();

            assertEquals(1, addParticipantActions.get());
        });
    }

    private void assertButtonOrder(Button... buttons) {
        Pane parent = (Pane) buttons[0].getParent();
        int firstButtonIndex =
                parent.getChildren().indexOf(buttons[0]);

        for (int index = 0; index < buttons.length; index++) {
            assertSame(parent, buttons[index].getParent());
            assertSame(
                    buttons[index],
                    parent.getChildren().get(firstButtonIndex + index));
        }
    }

    private void assertRightGap(
            Region cardPane,
            Button deleteButton) {
        StackPane container = new StackPane(cardPane);
        new Scene(container, 400, VIEWPORT_HEIGHT);

        container.applyCss();
        container.layout();

        Bounds cardBounds =
                cardPane.localToScene(cardPane.getBoundsInLocal());
        Bounds buttonBounds =
                deleteButton.localToScene(deleteButton.getBoundsInLocal());

        double rightGap =
                cardBounds.getMaxX() - buttonBounds.getMaxX();

        assertTrue(
                rightGap >= 16,
                "Delete buttons should have space before the scrollbar");
    }

    private void assertBrownBorder(
            Region cardPane,
            Button archiveButton) {
        StackPane container = new StackPane(cardPane);
        Scene scene = new Scene(container, 600, VIEWPORT_HEIGHT);
        scene.getStylesheets().add("view/DarkTheme.css");

        container.applyCss();
        container.layout();

        assertEquals(
                Color.web("#a56a43"),
                archiveButton.getBorder()
                        .getStrokes()
                        .getFirst()
                        .getTopStroke());
    }

    private void assertButtonsWithinViewport(
            Region cardPane,
            Button... buttons) {
        StackPane container = new StackPane(cardPane);
        Scene scene = new Scene(
                container,
                NARROW_VIEWPORT_WIDTH,
                VIEWPORT_HEIGHT);
        scene.getStylesheets().add("view/DarkTheme.css");

        container.applyCss();
        container.layout();

        for (Button button : buttons) {
            Bounds bounds =
                    button.localToScene(button.getBoundsInLocal());

            assertTrue(
                    bounds.getMinX() >= -BOUNDS_TOLERANCE,
                    button.getText() + " extends past the left edge");

            assertTrue(
                    bounds.getMaxX()
                            <= NARROW_VIEWPORT_WIDTH + BOUNDS_TOLERANCE,
                    button.getText() + " extends past the right edge");
        }
    }
}
