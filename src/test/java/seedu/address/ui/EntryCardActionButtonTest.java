package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.FxTestUtil.runOnFxThread;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import seedu.address.model.event.Event;
import seedu.address.model.event.EventId;
import seedu.address.model.mapper.Participation;

public class EntryCardActionButtonTest {

    @Test
    public void eventCard_archiveState_displaysSuffixWithoutChangingStoredName() throws Exception {
        runOnFxThread(() -> {
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard archived = new EventCard(event.withArchived(true), 1, () -> { }, () -> { });
            Label archivedName = (Label) archived.getRoot().lookup("#name");
            assertEquals("Workshop <archived>", archivedName.getText());
            EventCard active = new EventCard(event, 1, () -> { }, () -> { });
            assertEquals("Workshop", ((Label) active.getRoot().lookup("#name")).getText());
            assertEquals("Workshop", event.withArchived(true).getName());
        });
    }

    @Test
    public void personCard_actionButtons_runConfiguredActionsInEditDeleteOrder() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();
            PersonCard card = new PersonCard(
                    ALICE, 1, editActions::incrementAndGet, deleteActions::incrementAndGet);
            Button editButton = (Button) card.getRoot().lookup("#editButton");
            Button deleteButton = (Button) card.getRoot().lookup("#deleteButton");

            HBox cardPane = (HBox) card.getRoot();
            assertEquals("Edit", editButton.getText());
            assertSame(editButton, cardPane.getChildren().get(cardPane.getChildren().size() - 2));
            assertSame(deleteButton, cardPane.getChildren().get(cardPane.getChildren().size() - 1));
            assertRightGap(cardPane, deleteButton);
            editButton.fire();
            deleteButton.fire();

            assertEquals(1, editActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void participantCard_actionButtons_runConfiguredActionsInEditDeleteOrder() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            ParticipantCard card = new ParticipantCard(new Participation(event, ALICE),
                    editActions::incrementAndGet, deleteActions::incrementAndGet);
            Button editButton = (Button) card.getRoot().lookup("#editButton");
            Button deleteButton = (Button) card.getRoot().lookup("#deleteButton");

            HBox cardPane = (HBox) card.getRoot();
            assertEquals("Edit participation", editButton.getText());
            assertSame(editButton, cardPane.getChildren().get(cardPane.getChildren().size() - 2));
            assertSame(deleteButton, cardPane.getChildren().get(cardPane.getChildren().size() - 1));
            assertRightGap(cardPane, deleteButton);
            editButton.fire();
            deleteButton.fire();

            assertEquals(1, editActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void eventCard_actionButtons_runConfiguredActionsInRequestedOrder() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger addParticipantActions = new AtomicInteger();
            AtomicInteger editActions = new AtomicInteger();
            AtomicInteger deleteActions = new AtomicInteger();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard card = new EventCard(event, 1, addParticipantActions::incrementAndGet,
                    editActions::incrementAndGet, deleteActions::incrementAndGet);
            Button addParticipantButton = (Button) card.getRoot().lookup("#addParticipantButton");
            Button editButton = (Button) card.getRoot().lookup("#editButton");
            Button deleteButton = (Button) card.getRoot().lookup("#deleteButton");

            HBox cardPane = (HBox) card.getRoot();
            int actionStartIndex = cardPane.getChildren().size() - 3;
            assertEquals("Edit event", editButton.getText());
            assertSame(addParticipantButton, cardPane.getChildren().get(actionStartIndex));
            assertSame(editButton, cardPane.getChildren().get(actionStartIndex + 1));
            assertSame(deleteButton, cardPane.getChildren().get(cardPane.getChildren().size() - 1));
            assertRightGap(cardPane, deleteButton);
            addParticipantButton.fire();
            editButton.fire();
            deleteButton.fire();

            assertEquals(1, addParticipantActions.get());
            assertEquals(1, editActions.get());
            assertEquals(1, deleteActions.get());
        });
    }

    @Test
    public void eventCard_addParticipantButton_runsConfiguredAction() throws Exception {
        runOnFxThread(() -> {
            AtomicInteger addParticipantActions = new AtomicInteger();
            Event event = new Event(new EventId("E1"), "Workshop", 20, "Learn Java");
            EventCard card = new EventCard(event, 1, addParticipantActions::incrementAndGet, () -> { });
            Button addParticipantButton = (Button) card.getRoot().lookup("#addParticipantButton");

            assertEquals("Add participant", addParticipantButton.getText());
            addParticipantButton.fire();

            assertEquals(1, addParticipantActions.get());
        });
    }

    private void assertRightGap(HBox cardPane, Button deleteButton) {
        StackPane container = new StackPane(cardPane);
        new Scene(container, 400, 150);
        container.applyCss();
        container.layout();

        double rightGap = cardPane.getWidth() - deleteButton.getBoundsInParent().getMaxX();
        assertTrue(rightGap >= 16, "Delete buttons should have space before the scrollbar");
    }
}
